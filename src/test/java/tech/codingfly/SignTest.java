package tech.codingfly;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;
import tech.codingfly.core.constant.Constant;
import tech.codingfly.core.util.SignUtils;

import java.util.*;

public class SignTest {
    public static final HttpClient httpClient = HttpClients.createDefault();
    public static final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build();

    public static void main(String[] args) throws Exception {
        LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        logContext.getLogger("org.apache.http").setLevel(Level.INFO);
        String url = "http://localhost:8080/v1/post";
        for (int n = 0; n < 10; n++) {
            SortedMap<String, String> urlParams = new TreeMap();
            Map<String, Object> bodyParams = new HashMap();
            for (int i = 0; i < 3; i++) {
                String uuidMD5 = SignUtils.getMD5(UUID.randomUUID().toString());
                bodyParams.put(uuidMD5, uuidMD5);
                urlParams.put(SignUtils.getMD5(uuidMD5), uuidMD5);
            }
            httpPost(url, urlParams, bodyParams);
        }
    }

    public static void httpPost(String url, SortedMap<String, String> urlParams, Map<String, Object> bodyParams) throws Exception {
        String appId = "zs001";
        String timestamp = new Date().getTime()+"";
        String nonce = UUID.randomUUID().toString();

        StringBuilder sb = new StringBuilder(Constant.APP_ID+appId+Constant.TIME_STAMP+timestamp+Constant.NONCE+nonce);

        URIBuilder uriBuilder = new URIBuilder(url);
        for (String key:urlParams.keySet()) {
            String value = urlParams.get(key)==null? "":urlParams.get(key);
            uriBuilder.addParameter(key, value);
            sb.append(key+value);
        }

        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
        httpPost.addHeader(Constant.APP_ID, appId);
        httpPost.addHeader(Constant.TIME_STAMP, timestamp);
        httpPost.addHeader(Constant.NONCE, nonce);
        if (null != bodyParams) {
            httpPost.setEntity(new StringEntity(JSON.toJSONString(bodyParams), "utf-8"));
            sb.append(JSON.toJSONString(bodyParams, SerializerFeature.MapSortField));
        }
        sb.append(url+Constant.appIdMap.get(appId));
        System.out.println("加签字符串====>>>>>"+sb.toString());
        httpPost.addHeader(Constant.SIGN, SignUtils.getMD5(sb.toString()));
        CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpPost);
        // 请求发送成功，并得到响应
        String respStr = EntityUtils.toString(response.getEntity(), "utf-8");
        System.out.println(respStr);
        httpPost.releaseConnection();
    }

}
