package org.jflame.toolkit.key.serialnum;

import java.util.concurrent.TimeUnit;

/**
 * 单位时间内循环自增数.
 * <p>
 * 支持每天、每小时、每分、每秒重置自增 . 比如：new TimeUnitIncreaseNum(TimeUnit.DAYS)超过每天23:59:59后重置
 * 
 * @author yucan.zhang
 */
public final class TimeUnitIncreaseNum extends BaseIncreaseNum {

    private TimeUnit timeUnit;
    protected long lastTime;

    /**
     * 构造函数
     * 
     * @param timeUnit 时间单元
     */
    public TimeUnitIncreaseNum(TimeUnit timeUnit) {
        super();
        this.timeUnit = timeUnit;
        setLastTime();
    }

    /**
     * 构造函数
     * 
     * @param timeUnit 时间单元
     * @param initNum 初始值
     */
    public TimeUnitIncreaseNum(TimeUnit timeUnit, int initNum) {
        super(initNum);
        this.timeUnit = timeUnit;
    }

    @Override
    public synchronized long nextNum() {
        long curTime = millisToTimeunit(System.currentTimeMillis());
        if (curTime > lastTime) {
            sequence.set(initSeq);
            setLastTime();
        }
        return sequence.getAndIncrement();
    }

    private void setLastTime() {
        long now = System.currentTimeMillis();
        lastTime = millisToTimeunit(now);
    }

    private long millisToTimeunit(long milliseconds) {
        if (timeUnit == TimeUnit.DAYS) {
            return TimeUnit.MILLISECONDS.toDays(milliseconds);
        } else if (timeUnit == TimeUnit.HOURS) {
            return TimeUnit.MILLISECONDS.toHours(milliseconds);
        } else if (timeUnit == TimeUnit.MINUTES) {
            return TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        } else if (timeUnit == TimeUnit.SECONDS) {
            return TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        } else {
            throw new IllegalArgumentException("只支持\"天->秒\"时间单位");
        }
    }

    /*public static void main(String[] args) {
        TimeUnitIncreaseNum num = new TimeUnitIncreaseNum(TimeUnit.SECONDS);
        for (int i = 0; i < 10; i++) {
            System.out.println(num.nextNum());
        }
        try {
            Thread.sleep(999);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 5; i++) {
            System.out.println(num.nextNum());
        }
    }*/
}
