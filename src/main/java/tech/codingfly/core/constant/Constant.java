package tech.codingfly.core.constant;

import com.google.common.cache.*;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    public static LoadingCache<String, RateLimiter> ipRateLimiterCache;

    // 记录被请求消耗的流水号
    public static Cache<String, Boolean> hasUseReqNonceCache = CacheBuilder.newBuilder()
            .concurrencyLevel(8) // 设置并发级别为8，并发级别是指可以同时写缓存的线程数
            .expireAfterWrite(60*10, TimeUnit.SECONDS) // 当缓存项在指定的时间段内没有被写就会被回收
            .initialCapacity(1000) // 设置缓存容器的初始容量为1000
            .maximumSize(100000) // 设置缓存最大容量为100000，超过100000之后就会按照LRU最近虽少使用算法来移除缓存项
            .build();

    static {
        appIdMap.put("zs001", "asd123fhg3b7fgh7dfg");
        appIdMap.put("ls001", "hghfgh123btgfyh1212");
    }

    public static void initIpRateLimiterCache(Double oneIpRateLimiter) {
        if (ipRateLimiterCache==null) {
            ipRateLimiterCache =
                CacheBuilder.newBuilder()
                // 设置并发级别为8，并发级别是指可以同时写缓存的线程数
                .concurrencyLevel(8)
                // 当缓存项在指定的时间段内没有被读或写就会被回收
                .expireAfterAccess(120, TimeUnit.SECONDS)
                // 设置缓存容器的初始容量为10
                .initialCapacity(10)
                // 设置缓存最大容量为10000，超过10000之后就会按照LRU最近虽少使用算法来移除缓存项
                .maximumSize(10000)
                // 设置要统计缓存的命中率
//                .recordStats()
                // 设置缓存的移除通知
                .removalListener(new RemovalListener<String, RateLimiter>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, RateLimiter> notification) {
//                        System.out.println(notification.getKey() + " was removed, cause is " + notification.getCause());
                    }
                })
                // build方法中可以指定CacheLoader，在缓存不存在时通过CacheLoader的实现自动加载缓存
                .build(new CacheLoader<String, RateLimiter>() {
                    @Override
                    public RateLimiter load(String key) throws Exception {
                        return RateLimiter.create(oneIpRateLimiter);
                    }
                });
        }
    }

//    public static RedisTemplate redisTemplate;

    public static ApplicationContext applicationContext;

    public static Set<String> blackIpList = new HashSet();
    public static Set<Long> blackUserIds = new HashSet();

}
