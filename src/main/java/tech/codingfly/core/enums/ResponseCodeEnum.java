package tech.codingfly.core.enums;

public enum ResponseCodeEnum {
    AUTHORIZATION_ERR(401, "认证失败", "认证失败"),
    ERR(500, "服务异常", "服务异常"),
    NO_IP(600, "非法请求", "无IP地址"),
    SIGN_ERR(601, "签名校验失败", "头部参数必填校验失败"),
    APP_IP_ERR(602, "签名校验失败", "请求头appId不正确"),
    IP_BLACKLIST(603, "IP黑名单", "IP黑名单"),
    USER_ID_BLACKLIST(604, "userId黑名单", "userId黑名单");

    private Integer code;
    private String msg;
    private String realErr;

    ResponseCodeEnum(Integer code, String msg, String realErr) {
        this.code = code;
        this.msg = msg;
        this.realErr = realErr;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getRealErr() {
        return realErr;
    }

    public void setRealErr(String realErr) {
        this.realErr = realErr;
    }
}
