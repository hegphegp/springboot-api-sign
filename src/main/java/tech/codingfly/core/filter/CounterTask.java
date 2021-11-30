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
        long currentTimeMillis = System.currentTimeMillis();
        if ((currentTimeMillis-time)<=-1300 && (currentTimeMillis-time)>1300) {
            return;
        }
        long second = time/1000;
        int result = (int)second%3;
        switch (result) {

        }
        result = (int)(second/5)%2;
        if (result==0) {

        } else {

        }
        result = (int)(second/30)%2;
        if (result==0) {

        } else {

        }
        result = (int)(second/180)%2;
        if (result==0) {

        } else {

        }
        result = (int)(second/900)%2;
        if (result==0) {

        } else {

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
