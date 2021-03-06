package tech.codingfly.core.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import tech.codingfly.core.constant.Constant;
import tech.codingfly.core.enums.SignResultEnum;
import tech.codingfly.core.http.BodyReaderHttpServletRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class SignUtils {
    private static final Logger logger = LoggerFactory.getLogger(SignUtils.class);

    /**
     @Autowired
     private RedisTemplate redisTemplate;
     */

    /**
     * 校验Header上的参数-验证是否传入值
     * 有个很重要的一点，就是对此请求进行时间验证，如果大于10分钟表示此链接已经超时，防止别人拿到这个链接去请求。这个就是防止盗链。
     */
    public static SignResultEnum headerParamsIsValid(boolean checkToken, HttpServletRequest request) {
        String appId = request.getHeader(Constant.APP_ID);
        String sign = request.getHeader(Constant.SIGN);
        String valid = request.getHeader(Constant.VALID);
        String nonce = request.getHeader(Constant.NONCE);
        String timeStamp = request.getHeader(Constant.TIMESTAMP);
        if (StringUtils.isAnyBlank(appId, sign, nonce, timeStamp, valid)) {
            logger.debug("请求头参数appId，sign，valid，nonce，timeStamp存在为空");
            return SignResultEnum.ERROR;
        }
        // 因为 nonce 流水号存到内存里面，为减少内存使用，限制nonce最大长度为uuid转成两个long后再转base64转换的长度
        if (nonce.length()>23) {
            logger.debug("请求头nonce长度过长为空");
            return SignResultEnum.ERROR;
        }
        Boolean exists = Constant.hasUseReqNonceCache.getIfPresent(request.getHeader(Constant.APP_ID)+nonce);
        if (exists!=null) {
            logger.debug("请求被重放");
            return SignResultEnum.ERROR;
        }

        if (!StringUtils.isNumeric(timeStamp)) {
            logger.debug("请求头时间戳参数不是数字或者空");
            return SignResultEnum.ERROR;
        }

        long diff = System.currentTimeMillis() - Long.parseLong(timeStamp);
        if (diff > 1000 * 60 * 10 || diff < -1000 * 60 * 10) {
            logger.debug("请求头时间戳已失效");
            return SignResultEnum.ERROR;
        }

        String appSecret = Constant.appIdMap.get(request.getHeader(Constant.APP_ID));
        if (StringUtils.isBlank(appSecret)) {
            logger.debug("请求头appId不正确，获取的appSecret为空");
            return SignResultEnum.APPID_ERR;
        }
        if (checkToken) {
            String token = request.getHeader(Constant.TOKEN);
            if (StringUtils.isBlank(token)) {
                logger.debug("请求头token为空");
                return SignResultEnum.ERROR;
            }
            // md5(token+appSecret+时间戳)是否等于valid
            boolean result = valid.equals(DigestUtils.md5DigestAsHex((token + appSecret + timeStamp).getBytes()));
            return result? SignResultEnum.SUCCESS:SignResultEnum.ERROR;
        } else {
            // md5(appSecret+时间戳)是否等于valid
            boolean result = valid.equals(DigestUtils.md5DigestAsHex((appSecret + timeStamp).getBytes()));
            return result? SignResultEnum.SUCCESS:SignResultEnum.ERROR;
        }
    }

    public static boolean checkMd5Hash(String originStr, String sign) {
        return sign.equals(DigestUtils.md5DigestAsHex(originStr.getBytes()));
    }

    /**
     * value1value2value3
     * 按key排序顺序，拼接value字符串
     * @param request
     * @return
     */
    public static StringBuilder getHeaderSignParams(HttpServletRequest request) {
        return new StringBuilder(Constant.APP_ID+request.getHeader(Constant.APP_ID) +
                Constant.TIMESTAMP +request.getHeader(Constant.TIMESTAMP) +
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
                    // http://localhost:8080/v1/get?aa=11&aa=22&aa=33，url数组传参
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

