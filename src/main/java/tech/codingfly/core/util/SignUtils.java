package tech.codingfly.core.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import tech.codingfly.core.constant.Constant;
import tech.codingfly.core.http.BodyReaderHttpServletRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class SignUtils {
    private static final Logger logger = LoggerFactory.getLogger(SignUtils.class);
    private static MessageDigest md5;
    /**
     @Autowired
     private RedisTemplate redisTemplate;
     */

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 校验Header上的参数-验证是否传入值
     * 有个很重要的一点，就是对此请求进行时间验证，如果大于10分钟表示此链接已经超时，防止别人来到这个链接去请求。这个就是防止盗链。
     */
    public static boolean verifyHeaderParams(HttpServletRequest request) {
        if (StringUtils.isEmpty(request.getHeader(Constant.APP_ID)) ||
                StringUtils.isEmpty(request.getHeader(Constant.NONCE)) ||
                StringUtils.isEmpty(request.getHeader(Constant.SIGN))) {
            return false;
        }

        //时间戳,增加链接的有效时间,超过阈值,即失效
        String timeStamp = request.getHeader(Constant.TIME_STAMP);
        if (StringUtils.isEmpty(timeStamp) || StringUtils.isNumeric(timeStamp)==false) {
            return false;
        }

        //毫秒
        long diff = System.currentTimeMillis() - Long.parseLong(timeStamp);
        return (diff > 1000 * 60 * 10 || diff < -1000 * 60 * 10)? false:true;
    }

    public static String getMD5(String string) {
        try {
            byte[] bs = md5.digest(string.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(40);
            for (byte x : bs) {
                if ((x & 0xff) >> 4 == 0) {
                    sb.append("0").append(Integer.toHexString(x & 0xff));
                } else {
                    sb.append(Integer.toHexString(x & 0xff));
                }
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkMD5(String originStr, String sign) {
        return sign.equals(getMD5(originStr));
    }

    /**
     * value1value2value3
     * 按key排序顺序，拼接value字符串
     * @param request
     * @return
     */
    public static StringBuilder getHeaderSignParams(HttpServletRequest request) {
        return new StringBuilder(Constant.APP_ID+request.getHeader(Constant.APP_ID) +
                Constant.TIME_STAMP+request.getHeader(Constant.TIME_STAMP) +
                Constant.NONCE+request.getHeader(Constant.NONCE));
    }

    /**
     * 获取URL全部参数（会包含POST请求的form-data和x-www-form-urlencoded的参数）
     * @param request
     * @return
     */
    public static SortedMap<String, String> getRequestSortedParams(HttpServletRequest request) {
        SortedMap<String, String> sortedMap = new TreeMap();
        Map<String, String[]> requestParams = request.getParameterMap();
        if (!CollectionUtils.isEmpty(requestParams)) {
            for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
                if (entry.getValue().length>1) {
                    // http://localhost:8080/v1/get?aa=11&aa=22&aa=33，url传参，以后限制中间用逗号隔开，如果参数内容本身有逗号，会有问题
                    // sortedMap.put(entry.getKey(), String.join(",", new TreeSet(Arrays.asList(entry.getValue()))));
                    sortedMap.put(entry.getKey(), String.join(",", entry.getValue()));
                } else {
                    sortedMap.put(entry.getKey(), entry.getValue()[0]);
                }
            }
        }
        return sortedMap;
    }

    public static Map getBodyJsonParams(BodyReaderHttpServletRequestWrapper request) {
        byte[] bytes = request.getRequestBodyByte();
        String jsonStr = new String(bytes, 0, bytes.length);
        return JSONObject.parseObject(jsonStr);
    }

    /**
     * header的key+value拼接 加上 url的key+value 拼接
     * @param request
     * @return
     */
    public static StringBuilder combineHeaderRequestParam(HttpServletRequest request) {
        StringBuilder sb = getHeaderSignParams(request);
        Map<String, String> params = getRequestSortedParams(request);
        for (String key:params.keySet()) {
            sb.append(key+params.get(key));
        }
        return sb;
    }

}

