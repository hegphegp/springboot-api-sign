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
            recordTimes(ip, Constant.oneSecond1IpVisitCountCache, 50, Constant.oneMinuteBlackIpsCache,
                    userId, Constant.oneSecond1UserIdVisitCountCache, 10, Constant.oneMinuteBlackUserIdsCache);
        } else if (result==1) {
            recordTimes(ip, Constant.oneSecond2IpVisitCountCache, 50, Constant.oneMinuteBlackIpsCache,
                    userId, Constant.oneSecond2UserIdVisitCountCache, 10, Constant.oneMinuteBlackUserIdsCache);
        } else {
            recordTimes(ip, Constant.oneSecond3IpVisitCountCache, 50, Constant.oneMinuteBlackIpsCache,
                    userId, Constant.oneSecond3UserIdVisitCountCache, 10, Constant.oneMinuteBlackUserIdsCache);
        }

        result = (int)(second/5)%2;
        if (result==0) {
            recordTimes(ip, Constant.fiveSecond1IpVisitCountCache, 150, Constant.fiveMinuteBlackIpsCache,
                    userId, Constant.fiveSecond1UserIdVisitCountCache, 30, Constant.fiveMinuteBlackUserIdsCache);
        } else {
            recordTimes(ip, Constant.fiveSecond2IpVisitCountCache, 150, Constant.fiveMinuteBlackIpsCache,
                    userId, Constant.fiveSecond2UserIdVisitCountCache, 30, Constant.fiveMinuteBlackUserIdsCache);
        }

        result = (int)(second/30)%2;
        if (result==0) {
            recordTimes(ip, Constant.thirtySecond1IpVisitCountCache, 450, Constant.fifteenMinuteBlackIpsCache,
                    userId, Constant.thirtySecond1UserIdVisitCountCache, 90, Constant.fifteenMinuteBlackUserIdsCache);
        } else {
            recordTimes(ip, Constant.thirtySecond2IpVisitCountCache, 450, Constant.fifteenMinuteBlackIpsCache,
                    userId, Constant.thirtySecond2UserIdVisitCountCache, 90, Constant.fifteenMinuteBlackUserIdsCache);
        }

        result = (int)(second/180)%2;
        if (result==0) {
            recordTimes(ip, Constant.threeMinute1IpVisitCountCache, 1800, Constant.oneHourBlackIpsCache,
                    userId, Constant.threeMinute1UserIdVisitCountCache, 360, Constant.oneHourBlackUserIdsCache);
        } else {
            recordTimes(ip, Constant.threeMinute2IpVisitCountCache, 1800, Constant.oneHourBlackIpsCache,
                    userId, Constant.threeMinute2UserIdVisitCountCache, 360, Constant.oneHourBlackUserIdsCache);
        }

        result = (int)(second/900)%2;
        if (result==0) {
            recordTimes(ip, Constant.fifteenMinute1IpVisitCountCache, 7200, Constant.oneDayBlackIpsCache,
                    userId, Constant.fifteenMinute1UserIdVisitCountCache, 1200, Constant.oneDayBlackUserIdsCache);
        } else {
            recordTimes(ip, Constant.fifteenMinute1IpVisitCountCache, 7200, Constant.oneDayBlackIpsCache,
                    userId, Constant.fifteenMinute1UserIdVisitCountCache, 1200, Constant.oneDayBlackUserIdsCache);
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
