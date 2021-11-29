package tech.codingfly.core.filter;

/**
 * 这个task是当个线程池的线程执行的
 */
public class CounterTask implements Runnable {
    private Long time;
    private String ip;
    private Long userId;

    public CounterTask(String ip, Long userId) {
        this.ip = ip;
        this.userId = userId;
    }

    @Override
    public void run() {

    }

}
