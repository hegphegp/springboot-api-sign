package tech.codingfly.core.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.codingfly.core.constant.Constant;
import tech.codingfly.core.http.BodyReaderHttpServletRequestWrapper;
import tech.codingfly.core.util.ServletUtils;
import tech.codingfly.core.util.SignUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 验签工具
 * 请求URL 加上 header参数key和value拼接，加上url参数key和value拼接，加上按key排序号的请求体body的json字符串，合成一个字符串，然后校验md5
 */
public class SignAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(SignAuthFilter.class);

    /**
     @Autowired
     private RedisTemplate redisTemplate;
     */

    public static final Set<String> whiteList = new HashSet();

    // 可能包含请求体的method
    public static Set<String> hasBodyMethods = new HashSet(Arrays.asList("POST","PUT","PATCH","DELETE"));

    /**
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        ServletUtils.printAllHeaders();
        //白名单，不需要验证签名
        if (whiteList.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        //校验头部是否有验签参数
        boolean isValid = SignUtils.verifyHeaderParams(request);
        if (isValid) {
            //根据调用传递的appId获取对应的appSecret（应用密钥）
            String appSecret = Constant.appIdMap.get(request.getHeader(Constant.APP_ID));
            //appSecret（应用密钥）存在
            if (StringUtils.isNotEmpty(appSecret)) {
                StringBuilder sb = SignUtils.combineHeaderRequestParam(request);
                String contentType = request.getContentType()!=null? request.getContentType().toLowerCase():"";
                if (hasBodyMethods.contains(request.getMethod().toUpperCase()) && request.getContentLength()>0 && contentType.contains("application/json")) {
                    //包装HttpServletRequest对象，缓存body数据，再次读取的时候将缓存的值写出,解决HttpServetRequest读取body只能一次的问题
                    BodyReaderHttpServletRequestWrapper requestWrapper = new BodyReaderHttpServletRequestWrapper(request);
                    Map map = SignUtils.getBodyJsonParams(requestWrapper);
                    sb.append(JSON.toJSONString(map, SerializerFeature.MapSortField));
                    request = requestWrapper;
                }
                // 拼接完整的URL和私钥
                sb.append(ServletUtils.getOriginSchemeHostUrl(request)+appSecret);
                boolean isSigned = SignUtils.checkMD5(sb.toString(), request.getHeader(Constant.SIGN));
                // appId+timeStamp+nonce是否被攻击重复了，看redis是否有这个key
                if (isSigned) {
//                    logger.info("签名通过");
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }
//        logger.info("签名校验失败");
        assemblyResponse(request, response);
    }

    private void assemblyResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(500);
        String url = ServletUtils.getOriginSchemeHostUrl(request);
        Map map = new HashMap() {{
            put("code", 500);
            put("msg", "签名校验失败");
            put("url", url);
        }};
        response.setContentType("application/json;charset=utf-8");
        response.getOutputStream().write(JSON.toJSONString(map).getBytes());
    }

}

