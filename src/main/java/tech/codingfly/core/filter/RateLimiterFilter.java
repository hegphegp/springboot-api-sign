package tech.codingfly.core.filter;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tech.codingfly.core.constant.Constant;
import static tech.codingfly.core.constant.Constant.*;

import tech.codingfly.core.enums.ResponseCodeEnum;
import tech.codingfly.core.enums.SignResultEnum;
import tech.codingfly.core.model.User;
import tech.codingfly.core.util.ServletUtils;
import tech.codingfly.core.util.SignUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 限流的过滤器优先排在第一位
 * 世纪无奈, @Order执行顺序可能配置会不生效, 决定用FilterRegistrationBean配置
 * @author hgp
 * @date 20-5-27
 */
public class RateLimiterFilter extends OncePerRequestFilter {
    public final static ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Logger logger = LoggerFactory.getLogger(RateLimiterFilter.class);

    private byte[] errorMsgBytes = "{\"code\":500,\"msg\":\"限流\"}".getBytes();

    //存入URL 和 rateLimiter 的实列，保证 每个请求都是单列的
    private Map<String, RateLimiter> urlRateMap = new ConcurrentHashMap();
    private RateLimiter globalRateLimiter = null;
    private RateLimiter otherUrlsRateLimiter = RateLimiter.create(100d);

    public final static PathMatcher pathMatcher = new AntPathMatcher();
    // URL有通配符, 或者占位符的Map
    public final static Set<String> patternUrls = new HashSet();
    // 完全匹配的URL的Map
    public final static Set<String> directUrls = new HashSet();

    // 白名单，不需要验证token，这种URL禁止使用通配符URL，例如/user/{userId}
    public static final Set<String> tokenWhiteList = new HashSet();

    public RateLimiterFilter(Double oneSecondRateLimiter, Double oneSecondOneUrlRateLimiter,
                             Double oneSecondOneIpRateLimiter, ApplicationContext applicationContext) {
        // 初始化IP访问次数限制的工具类
        Constant.initIpRateLimiterCache(oneSecondOneIpRateLimiter);
        globalRateLimiter = RateLimiter.create(oneSecondRateLimiter);
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();// 就是这个
        for (RequestMappingInfo rmi : handlerMethods.keySet()) {
            Set<String> set = rmi.getPatternsCondition().getPatterns();
            for (String url:set) {
                Set<String> methods = getMethodStr(rmi.getMethodsCondition().getMethods());
                for (String method:methods) {
                    if (pathMatcher.isPattern(url)==false && !url.contains("{") && !url.contains("}")) {
                        directUrls.add(method+" "+url);
                    } else {
                        patternUrls.add(method+" "+url);
                    }
                    urlRateMap.put(method+" "+url, RateLimiter.create(oneSecondOneUrlRateLimiter));
                }
            }
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = ServletUtils.getOriginIp(request);
        if (StringUtils.isBlank(ip)) {
            ServletUtils.assemblyResponse(ResponseCodeEnum.NO_IP, response);
            return;
        }

        boolean ipBlackList = blackListIp(ip);
        // 记录IP访问次数
        VisitCounter.recordIpTimes(System.currentTimeMillis(), ip);
        if (ipBlackList) {
            ServletUtils.assemblyResponse(ResponseCodeEnum.IP_BLACKLIST, response);
            return;
        }

        boolean needCheckToken = !tokenWhiteList.contains(request.getRequestURI());
        //校验头部是否有验签参数
        SignResultEnum signResult = SignUtils.headerParamsIsValid(needCheckToken, request);
        if (signResult==SignResultEnum.ERROR) {
            ServletUtils.assemblyResponse(ResponseCodeEnum.SIGN_ERR, response);
            return;
        }
        if (signResult==SignResultEnum.APPID_ERR) {
            ServletUtils.assemblyResponse(ResponseCodeEnum.APP_IP_ERR, response);
            return;
        }
        if (needCheckToken) {
            // 记录userId访问次数
            String token = request.getHeader(Constant.TOKEN);
            User user = Constant.userTokenCache.getIfPresent(token);
            if (user==null) {
                ServletUtils.assemblyResponse(ResponseCodeEnum.AUTHORIZATION_ERR, response);
                logger.error("token无效");
                return;
            }

            Long userId = user.getId();
            boolean userIdBlackList = blackListUserId(userId);
            // 记录userId访问次数
            VisitCounter.recordUserIdTimes(System.currentTimeMillis(), userId);
            if (userIdBlackList) {
                ServletUtils.assemblyResponse(ResponseCodeEnum.USER_ID_BLACKLIST, response);
                return;
            }
        }

        // 全局限流，等待5毫秒
        boolean result = globalRateLimiter.tryAcquire(5, TimeUnit.MICROSECONDS);
        if (result==false) {
            ServletUtils.assemblyResponse(response, errorMsgBytes);
            logger.error("触发全局限流");
            return;
        }

        try {
            result = Constant.ipRateLimiterCache.get(ip).tryAcquire(5, TimeUnit.MICROSECONDS);
            if (result==false) {
                ServletUtils.assemblyResponse(response, errorMsgBytes);
                logger.error("触发单个IP的访问限流");
                return;
            }
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
            ServletUtils.assemblyResponse(ResponseCodeEnum.ERR, response);
            return;
        }
        String requestURI = request.getRequestURI();
//        logger.debug("请求的URL是  "+request.getMethod()+" "+requestURI);
        String methodUrl = null;
        if (directUrls.contains(request.getMethod()+" "+requestURI)) { //对该URL镜像权限拦截
//            logger.debug("最优匹配的URL是 "+request.getMethod()+" "+requestURI);
            methodUrl = request.getMethod()+" "+requestURI;
        } else { // 可能是通配符动态URL
            List<String> matches = getMatchingPatterns(request.getMethod()+" "+requestURI);
            if (ObjectUtils.isNotEmpty(matches)) {
//                logger.debug("匹配到的URL有 "+matches);
//                logger.debug("最优匹配的URL是 "+matches.get(0));
                methodUrl = matches.get(0);
            }
        }
        RateLimiter urlRateLimiter = methodUrl!=null? urlRateMap.get(methodUrl):otherUrlsRateLimiter;
        // 等待5毫秒
        result = urlRateLimiter.tryAcquire(5, TimeUnit.MICROSECONDS);
        if (result==false) {
            ServletUtils.assemblyResponse(response, errorMsgBytes);
            logger.error("触发URL限流");
            return;
        }
        filterChain.doFilter(request, response);
    }

    public boolean blackListIp(String ip) {
        if (oneMinuteBlackIpsCache.getIfPresent(ip)!=null) {
            return true;
        }
        if (fiveMinuteBlackIpsCache.getIfPresent(ip)!=null) {
            return true;
        }
        if (fifteenMinuteBlackIpsCache.getIfPresent(ip)!=null) {
            return true;
        }
        if (oneHourBlackIpsCache.getIfPresent(ip)!=null) {
            return true;
        }
        if (oneDayBlackIpsCache.getIfPresent(ip)!=null) {
            return true;
        }
        return false;
    }

    public boolean blackListUserId(Long userId) {
        if (oneMinuteBlackUserIdsCache.getIfPresent(userId)!=null) {
            return true;
        }
        if (fiveMinuteBlackUserIdsCache.getIfPresent(userId)!=null) {
            return true;
        }
        if (fifteenMinuteBlackUserIdsCache.getIfPresent(userId)!=null) {
            return true;
        }
        if (oneHourBlackUserIdsCache.getIfPresent(userId)!=null) {
            return true;
        }
        if (oneDayBlackUserIdsCache.getIfPresent(userId)!=null) {
            return true;
        }
        return false;
    }

    public List<String> getMatchingPatterns(String methodAndRequestURI) {
        List<String> matches = new ArrayList<>();
        for (String pattern :patternUrls) {
            String match = getMatchingPattern(pattern, methodAndRequestURI);
            if (match != null) {
                matches.add(match);
            }
        }
        if (matches.size() > 1) {
            matches.sort(pathMatcher.getPatternComparator(methodAndRequestURI));
        }
        return matches;
    }

    private String getMatchingPattern(String pattern, String methodAndRequestURI) {
        if (pattern.equals(methodAndRequestURI)) {
            return pattern;
        }
        if (pathMatcher.match(pattern, methodAndRequestURI)) {
            return pattern;
        }
        if (!pattern.endsWith("/") && pathMatcher.match(pattern + "/", methodAndRequestURI)) {
            return pattern + "/";
        }
        return null;
    }

    public Set<String> getMethodStr(Set<RequestMethod> set) {
        return ObjectUtils.isEmpty(set)? new HashSet(Arrays.asList("GET","HEAD","POST","PUT","PATCH","DELETE","OPTIONS","TRACE")):set.stream().map(o->o.name()).collect(Collectors.toSet());
    }

}
