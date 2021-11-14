package tech.codingfly.core.constant;

import java.util.HashMap;
import java.util.Map;

public class Constant {
    /**
     * 应用id
     */
    public static final String APP_ID ="appId";
    /**
     * 时间戳,增加链接的有效时间,超过阈值,即失效
     */
    public static final String TIME_STAMP ="timeStamp";
    /**
     *签名
     */
    public static final String SIGN ="sign";
    /**
     * 临时流水号/随机串 ,至少为10位 ，有效期内防重复提交
     */
    public static final String NONCE ="nonce";

    public static Map<String, String> appIdMap = new HashMap();

    static {
        appIdMap.put("zs001", "asd123fhg3b7fgh7dfg");
        appIdMap.put("ls001", "hghfgh123btgfyh1212");
    }

}
