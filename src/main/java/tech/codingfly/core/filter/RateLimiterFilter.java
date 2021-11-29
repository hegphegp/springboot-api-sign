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
import tech.codingfly.core.util.ServletUtils;

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

    private PathMatcher pathMatcher = new AntPathMatcher();
    // URL有通配符, 或者占位符的Map
    private Set<String> patternUrls = new HashSet();
    // 完全匹配的URL的Map
    private Set<String> directUrls = new HashSet();

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
        String ip = ServletUtils.getCurrentRequestOriginIp(request);
        // 全局限流，等待5毫秒
        boolean result = globalRateLimiter.tryAcquire(5, TimeUnit.MICROSECONDS);
        if (result==false) {
            assemblyResponse(response);
            logger.error("触发全局限流");
            return;
        }

        if (StringUtils.isNotBlank(ip)) {
            try {
                result = Constant.ipRateLimiterCache.get(ip).tryAcquire(5, TimeUnit.MICROSECONDS);
                if (result==false) {
                    assemblyResponse(response);
                    logger.error("触发单个IP的访问限流");
                    return;
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
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
            assemblyResponse(response);
            logger.error("触发URL限流");
            return;
        }
        filterChain.doFilter(request, response);
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

    private void assemblyResponse(HttpServletResponse response) throws IOException {
        response.setStatus(500);
        response.setContentType("application/json; charset=utf-8");
        response.getOutputStream().write(errorMsgBytes);
    }

}
