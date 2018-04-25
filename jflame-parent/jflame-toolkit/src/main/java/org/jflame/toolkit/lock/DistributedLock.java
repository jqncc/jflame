package org.jflame.toolkit.lock;

import java.util.concurrent.TimeUnit;

public interface DistributedLock {

    /**
     * 尝试加锁,无阻塞,获取失败直接返回
     * 
     * @return true取锁成功
     */
    public boolean tryLock();

    /**
     * 尝试加锁,阻塞指定时间后返回
     * 
     * @param waitTime 阻塞时间
     * @param timeUnit 阻塞时间单位
     * @return true取锁成功
     */
    public boolean tryLock(long waitTime, TimeUnit timeUnit);

    /**
     * 加锁,直接成功
     */
    public void lock();

    /**
     * 解锁
     */
    public void unlock();

}
