package tech.codingfly.core.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.codingfly.core.constant.Constant;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 访问次数统计
 */
public class VisitCounter {

    private static Logger logger = LoggerFactory.getLogger(VisitCounter.class);

    public static void recordIpTimes(long time, String ip) {
        try {
            long second = time / 1000;
            int result = (int) second % 3;
            if (result == 0) {
                recordIpTimes(ip, Constant.oneSecond1IpVisitCountCache, Constant.oneSecondIpMaxVisitCount, Constant.oneMinuteBlackIpsCache);
            } else if (result == 1) {
                recordIpTimes(ip, Constant.oneSecond2IpVisitCountCache, Constant.oneSecondIpMaxVisitCount, Constant.oneMinuteBlackIpsCache);
            } else {
                recordIpTimes(ip, Constant.oneSecond3IpVisitCountCache, Constant.oneSecondIpMaxVisitCount, Constant.oneMinuteBlackIpsCache);
            }

            result = (int) (second / 5) % 2;
            if (result == 0) {
                recordIpTimes(ip, Constant.fiveSecond1IpVisitCountCache, Constant.fiveSecondIpMaxVisitCount, Constant.fiveMinuteBlackIpsCache);
            } else {
                recordIpTimes(ip, Constant.fiveSecond2IpVisitCountCache, Constant.fiveSecondIpMaxVisitCount, Constant.fiveMinuteBlackIpsCache);
            }

            result = (int) (second / 30) % 2;
            if (result == 0) {
                recordIpTimes(ip, Constant.thirtySecond1IpVisitCountCache, Constant.thirtySecondIpMaxVisitCount, Constant.fifteenMinuteBlackIpsCache);
            } else {
                recordIpTimes(ip, Constant.thirtySecond2IpVisitCountCache, Constant.thirtySecondIpMaxVisitCount, Constant.fifteenMinuteBlackIpsCache);
            }

            result = (int) (second / 180) % 2;
            if (result == 0) {
                recordIpTimes(ip, Constant.threeMinute1IpVisitCountCache, Constant.threeMinuteIpMaxVisitCount, Constant.oneHourBlackIpsCache);
            } else {
                recordIpTimes(ip, Constant.threeMinute2IpVisitCountCache, Constant.threeMinuteIpMaxVisitCount, Constant.oneHourBlackIpsCache);
            }

            result = (int) (second / 900) % 2;
            if (result == 0) {
                recordIpTimes(ip, Constant.fifteenMinute1IpVisitCountCache, Constant.fifteenMinuteIpMaxVisitCount, Constant.oneDayBlackIpsCache);
            } else {
                recordIpTimes(ip, Constant.fifteenMinute2IpVisitCountCache, Constant.fifteenMinuteIpMaxVisitCount, Constant.oneDayBlackIpsCache);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void recordUserIdTimes(long time, Long userId) {
        try {
            long second = time / 1000;
            int result = (int) second % 3;
            if (result == 0) {
                recordUserIdTimes(userId, Constant.oneSecond1UserIdVisitCountCache, Constant.oneSecondUserIdMaxVisitCount, Constant.oneMinuteBlackUserIdsCache);
            } else if (result == 1) {
                recordUserIdTimes(userId, Constant.oneSecond2UserIdVisitCountCache, Constant.oneSecondUserIdMaxVisitCount, Constant.oneMinuteBlackUserIdsCache);
            } else {
                recordUserIdTimes(userId, Constant.oneSecond3UserIdVisitCountCache, Constant.oneSecondUserIdMaxVisitCount, Constant.oneMinuteBlackUserIdsCache);
            }

            result = (int) (second / 5) % 2;
            if (result == 0) {
                recordUserIdTimes(userId, Constant.fiveSecond1UserIdVisitCountCache, Constant.fiveSecondUserIdMaxVisitCount, Constant.fiveMinuteBlackUserIdsCache);
            } else {
                recordUserIdTimes(userId, Constant.fiveSecond2UserIdVisitCountCache, Constant.fiveSecondUserIdMaxVisitCount, Constant.fiveMinuteBlackUserIdsCache);
            }

            result = (int) (second / 30) % 2;
            if (result == 0) {
                recordUserIdTimes(userId, Constant.thirtySecond1UserIdVisitCountCache, Constant.thirtySecondUserIdMaxVisitCount, Constant.fifteenMinuteBlackUserIdsCache);
            } else {
                recordUserIdTimes(userId, Constant.thirtySecond2UserIdVisitCountCache, Constant.thirtySecondUserIdMaxVisitCount, Constant.fifteenMinuteBlackUserIdsCache);
            }

            result = (int) (second / 180) % 2;
            if (result == 0) {
                recordUserIdTimes(userId, Constant.threeMinute1UserIdVisitCountCache, Constant.threeMinuteUserIdMaxVisitCount, Constant.oneHourBlackUserIdsCache);
            } else {
                recordUserIdTimes(userId, Constant.threeMinute2UserIdVisitCountCache, Constant.threeMinuteUserIdMaxVisitCount, Constant.oneHourBlackUserIdsCache);
            }

            result = (int) (second / 900) % 2;
            if (result == 0) {
                recordUserIdTimes(userId, Constant.fifteenMinute1UserIdVisitCountCache, Constant.fifteenMinuteUserIdMaxVisitCount, Constant.oneDayBlackUserIdsCache);
            } else {
                recordUserIdTimes(userId, Constant.fifteenMinute2UserIdVisitCountCache, Constant.fifteenMinuteUserIdMaxVisitCount, Constant.oneDayBlackUserIdsCache);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void recordIpTimes(String ip, LoadingCache<String, AtomicInteger> ipVisitCountCache, Integer ipMaxVisitCount, Cache<String, Boolean> blackIpsCache) throws ExecutionException {
        AtomicInteger atomicInteger = ipVisitCountCache.get(ip);
        int count = atomicInteger.incrementAndGet();
        if (count>=ipMaxVisitCount) {
            blackIpsCache.put(ip, true);
        }
    }

    private static void recordUserIdTimes(Long userId, LoadingCache<Long, AtomicInteger> userIdVisitCountCache, Integer userIdMaxVisitCount, Cache<Long, Boolean> blackUserIdsCache) throws ExecutionException {
        AtomicInteger atomicInteger = userIdVisitCountCache.get(userId);
        int count = atomicInteger.incrementAndGet();
        if (count>=userIdMaxVisitCount) {
            blackUserIdsCache.put(userId, true);
        }
    }

    public static void main(String[] args) {
        long currentTimeMillis = System.currentTimeMillis();
        long second = currentTimeMillis/1000;
        System.out.println(second);
        int result = (int)second%3;
        System.out.println(result);
    }

}
