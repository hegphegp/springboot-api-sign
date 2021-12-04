package tech.codingfly.core.constant;

import com.google.common.cache.*;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Constant {
    /** 应用id */
    public static final String APP_ID ="appId";
    /** 时间戳,增加链接的有效时间,超过阈值,即失效 */
    public static final String TIME_STAMP ="timeStamp";
    /** 签名 */
    public static final String SIGN ="sign";
    /** 临时流水号/随机串 ,至少为10位 ，有效期内防重复提交 */
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

    public static Cache<String, Boolean> oneMinuteBlackIpsCache = buildBlackIpsCache(60);
    public static Cache<String, Boolean> fiveMinuteBlackIpsCache = buildBlackIpsCache(300);
    public static Cache<String, Boolean> fifteenMinuteBlackIpsCache = buildBlackIpsCache(900);
    public static Cache<String, Boolean> oneHourBlackIpsCache = buildBlackIpsCache(3600);
    public static Cache<String, Boolean> oneDayBlackIpsCache = buildBlackIpsCache(3600*24);

    public static Cache<Long, Boolean> oneMinuteBlackUserIdsCache = buildBlackUserIdsCache(60);
    public static Cache<Long, Boolean> fiveMinuteBlackUserIdsCache = buildBlackUserIdsCache(300);
    public static Cache<Long, Boolean> fifteenMinuteBlackUserIdsCache = buildBlackUserIdsCache(900);
    public static Cache<Long, Boolean> oneHourBlackUserIdsCache = buildBlackUserIdsCache(3600);
    public static Cache<Long, Boolean> oneDayBlackUserIdsCache = buildBlackUserIdsCache(3600*24);

    // 每次缩减为上一次次数 × 时间倍数 × 60%
    public static Integer oneSecondIpMaxVisitCount = 50;
    public static Integer fiveSecondIpMaxVisitCount = 150;
    public static Integer thirtySecondIpMaxVisitCount = 540;
    public static Integer threeMinuteIpMaxVisitCount = 1944;
    public static Integer fifteenMinuteIpMaxVisitCount = 5832;

    public static Integer oneSecondUserIdMaxVisitCount = 15;
    public static Integer fiveSecondUserIdMaxVisitCount = 45;
    public static Integer thirtySecondUserIdMaxVisitCount = 162;
    public static Integer threeMinuteUserIdMaxVisitCount = 583;
    public static Integer fifteenMinuteUserIdMaxVisitCount = 1750;

    // 记录访问次数
    public static Cache<String, Integer> oneSecond1IpVisitCountCache = buildIpVisitCountCache(1);
    public static Cache<String, Integer> oneSecond2IpVisitCountCache = buildIpVisitCountCache(1);
    public static Cache<String, Integer> oneSecond3IpVisitCountCache = buildIpVisitCountCache(1);
    public static Cache<String, Integer> fiveSecond1IpVisitCountCache = buildIpVisitCountCache(4);
    public static Cache<String, Integer> fiveSecond2IpVisitCountCache = buildIpVisitCountCache(4);
    public static Cache<String, Integer> thirtySecond1IpVisitCountCache = buildIpVisitCountCache(28);
    public static Cache<String, Integer> thirtySecond2IpVisitCountCache = buildIpVisitCountCache(28);
    public static Cache<String, Integer> threeMinute1IpVisitCountCache = buildIpVisitCountCache(178);
    public static Cache<String, Integer> threeMinute2IpVisitCountCache = buildIpVisitCountCache(178);
    public static Cache<String, Integer> fifteenMinute1IpVisitCountCache = buildIpVisitCountCache(898);
    public static Cache<String, Integer> fifteenMinute2IpVisitCountCache = buildIpVisitCountCache(898);

    public static Cache<Long, Integer> oneSecond1UserIdVisitCountCache = buildUserIdVisitCountCache(1);
    public static Cache<Long, Integer> oneSecond2UserIdVisitCountCache = buildUserIdVisitCountCache(1);
    public static Cache<Long, Integer> oneSecond3UserIdVisitCountCache = buildUserIdVisitCountCache(1);
    public static Cache<Long, Integer> fiveSecond1UserIdVisitCountCache = buildUserIdVisitCountCache(4);
    public static Cache<Long, Integer> fiveSecond2UserIdVisitCountCache = buildUserIdVisitCountCache(4);
    public static Cache<Long, Integer> thirtySecond1UserIdVisitCountCache = buildUserIdVisitCountCache(28);
    public static Cache<Long, Integer> thirtySecond2UserIdVisitCountCache = buildUserIdVisitCountCache(28);
    public static Cache<Long, Integer> threeMinute1UserIdVisitCountCache = buildUserIdVisitCountCache(178);
    public static Cache<Long, Integer> threeMinute2UserIdVisitCountCache = buildUserIdVisitCountCache(178);
    public static Cache<Long, Integer> fifteenMinute1UserIdVisitCountCache = buildUserIdVisitCountCache(895);
    public static Cache<Long, Integer> fifteenMinute2UserIdVisitCountCache = buildUserIdVisitCountCache(895);

    static {
        appIdMap.put("zs001", "asd123fhg3b7fgh7dfg");
        appIdMap.put("ls001", "hghfgh123btgfyh1212");
    }

    public static Cache<Long, Boolean> buildBlackUserIdsCache(int expireSecond) {
        return CacheBuilder.newBuilder()
                .concurrencyLevel(1) // 设置并发级别为1，并发级别是指可以同时写缓存的线程数
                .expireAfterWrite(expireSecond, TimeUnit.SECONDS) // 当缓存项在指定的时间段内没有被写就会被回收
                .initialCapacity(32) // 设置缓存容器的初始容量为32
                .maximumSize(10000) // 设置缓存最大容量为10000，超过10000之后就会按照LRU最近虽少使用算法来移除缓存项
                .build();
    }

    public static Cache<String, Boolean> buildBlackIpsCache(int expireSecond) {
        return CacheBuilder.newBuilder()
                .concurrencyLevel(1) // 设置并发级别为1，并发级别是指可以同时写缓存的线程数
                .expireAfterWrite(expireSecond, TimeUnit.SECONDS) // 当缓存项在指定的时间段内没有被写就会被回收
                .initialCapacity(32) // 设置缓存容器的初始容量为32
                .maximumSize(10000) // 设置缓存最大容量为10000，超过10000之后就会按照LRU最近虽少使用算法来移除缓存项
                .build();
    }

    public static Cache<String, Integer> buildIpVisitCountCache(int expireSecond) {
        return CacheBuilder.newBuilder()
                .concurrencyLevel(1) // 设置并发级别为1，并发级别是指可以同时写缓存的线程数
                .expireAfterWrite(expireSecond, TimeUnit.SECONDS) // 当缓存项在指定的时间段内没有被写就会被回收
                .initialCapacity(32) // 设置缓存容器的初始容量为32
                .maximumSize(10000) // 设置缓存最大容量为10000，超过10000之后就会按照LRU最近虽少使用算法来移除缓存项
                .build();
    }

    public static Cache<Long, Integer> buildUserIdVisitCountCache(int expireSecond) {
        return CacheBuilder.newBuilder()
                .concurrencyLevel(1) // 设置并发级别为1，并发级别是指可以同时写缓存的线程数
                .expireAfterWrite(expireSecond, TimeUnit.SECONDS) // 当缓存项在指定的时间段内没有被写就会被回收
                .initialCapacity(32) // 设置缓存容器的初始容量为32
                .maximumSize(10000) // 设置缓存最大容量为10000，超过10000之后就会按照LRU最近虽少使用算法来移除缓存项
                .build();
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

}
