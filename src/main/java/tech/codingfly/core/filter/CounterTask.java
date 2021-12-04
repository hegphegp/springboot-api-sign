package tech.codingfly.core.filter;

import com.google.common.cache.Cache;
import org.springframework.util.StringUtils;
import tech.codingfly.core.constant.Constant;

/**
 * 这个task是当个线程池的线程执行的
 */
public class CounterTask implements Runnable {

    private Long time;
    private String ip;
    private Long userId;

    public CounterTask(long time, String ip, Long userId) {
        this.time = time;
        this.ip = ip;
        this.userId = userId;
    }

    @Override
    public void run() {
        long currentTimeMillis = System.currentTimeMillis();
        if ((currentTimeMillis-time)<=-1300 && (currentTimeMillis-time)>1300) {
            return;
        }
        long second = time/1000;
        int result = (int)second%3;
        if (result==0) {
            recordTimes(ip, Constant.oneSecond1IpVisitCountCache, Constant.oneSecondIpMaxVisitCount, Constant.oneMinuteBlackIpsCache,
                    userId, Constant.oneSecond1UserIdVisitCountCache, Constant.oneSecondUserIdMaxVisitCount, Constant.oneMinuteBlackUserIdsCache);
        } else if (result==1) {
            recordTimes(ip, Constant.oneSecond2IpVisitCountCache, Constant.oneSecondIpMaxVisitCount, Constant.oneMinuteBlackIpsCache,
                    userId, Constant.oneSecond2UserIdVisitCountCache, Constant.oneSecondUserIdMaxVisitCount, Constant.oneMinuteBlackUserIdsCache);
        } else {
            recordTimes(ip, Constant.oneSecond3IpVisitCountCache, Constant.oneSecondIpMaxVisitCount, Constant.oneMinuteBlackIpsCache,
                    userId, Constant.oneSecond3UserIdVisitCountCache, Constant.oneSecondUserIdMaxVisitCount, Constant.oneMinuteBlackUserIdsCache);
        }

        result = (int)(second/5)%2;
        if (result==0) {
            recordTimes(ip, Constant.fiveSecond1IpVisitCountCache, Constant.fiveSecondIpMaxVisitCount, Constant.fiveMinuteBlackIpsCache,
                    userId, Constant.fiveSecond1UserIdVisitCountCache, Constant.fiveSecondUserIdMaxVisitCount, Constant.fiveMinuteBlackUserIdsCache);
        } else {
            recordTimes(ip, Constant.fiveSecond2IpVisitCountCache, Constant.fiveSecondIpMaxVisitCount, Constant.fiveMinuteBlackIpsCache,
                    userId, Constant.fiveSecond2UserIdVisitCountCache, Constant.fiveSecondUserIdMaxVisitCount, Constant.fiveMinuteBlackUserIdsCache);
        }

        result = (int)(second/30)%2;
        if (result==0) {
            recordTimes(ip, Constant.thirtySecond1IpVisitCountCache, Constant.thirtySecondIpMaxVisitCount, Constant.fifteenMinuteBlackIpsCache,
                    userId, Constant.thirtySecond1UserIdVisitCountCache, Constant.thirtySecondUserIdMaxVisitCount, Constant.fifteenMinuteBlackUserIdsCache);
        } else {
            recordTimes(ip, Constant.thirtySecond2IpVisitCountCache, Constant.thirtySecondIpMaxVisitCount, Constant.fifteenMinuteBlackIpsCache,
                    userId, Constant.thirtySecond2UserIdVisitCountCache, Constant.thirtySecondUserIdMaxVisitCount, Constant.fifteenMinuteBlackUserIdsCache);
        }

        result = (int)(second/180)%2;
        if (result==0) {
            recordTimes(ip, Constant.threeMinute1IpVisitCountCache, Constant.threeMinuteIpMaxVisitCount, Constant.oneHourBlackIpsCache,
                    userId, Constant.threeMinute1UserIdVisitCountCache, Constant.threeMinuteUserIdMaxVisitCount, Constant.oneHourBlackUserIdsCache);
        } else {
            recordTimes(ip, Constant.threeMinute2IpVisitCountCache, Constant.threeMinuteIpMaxVisitCount, Constant.oneHourBlackIpsCache,
                    userId, Constant.threeMinute2UserIdVisitCountCache, Constant.threeMinuteUserIdMaxVisitCount, Constant.oneHourBlackUserIdsCache);
        }

        result = (int)(second/900)%2;
        if (result==0) {
            recordTimes(ip, Constant.fifteenMinute1IpVisitCountCache, Constant.fifteenMinuteIpMaxVisitCount, Constant.oneDayBlackIpsCache,
                    userId, Constant.fifteenMinute1UserIdVisitCountCache, Constant.fifteenMinuteUserIdMaxVisitCount, Constant.oneDayBlackUserIdsCache);
        } else {
            recordTimes(ip, Constant.fifteenMinute2IpVisitCountCache, Constant.fifteenMinuteIpMaxVisitCount, Constant.oneDayBlackIpsCache,
                    userId, Constant.fifteenMinute2UserIdVisitCountCache, Constant.fifteenMinuteUserIdMaxVisitCount, Constant.oneDayBlackUserIdsCache);
        }
    }

    public static void recordTimes(String ip, Cache<String, Integer> ipVisitCountCache, Integer ipMaxVisitCount, Cache<String, Boolean> blackIpsCache,
                                   Long userId, Cache<Long, Integer> userIdVisitCountCache, Integer userIdMaxVisitCount, Cache<Long, Boolean> blackUserIdsCache) {
        if (StringUtils.hasText(ip)) {
            Integer count = ipVisitCountCache.getIfPresent(ip);
            ipVisitCountCache.put(ip, ++count);
            if (count>=ipMaxVisitCount) {
                blackIpsCache.put(ip, true);
            }
        }
        if (userId!=null) {
            Integer count = userIdVisitCountCache.getIfPresent(userId);
            userIdVisitCountCache.put(userId, ++count);
            if (count>=userIdMaxVisitCount) {
                blackUserIdsCache.put(userId, true);
            }
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
