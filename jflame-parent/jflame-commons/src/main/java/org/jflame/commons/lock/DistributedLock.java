package org.jflame.commons.lock;

/**
 * 分布式锁
 * 
 * @author yucan.zhang
 */
public interface DistributedLock {

    /**
     * 获取锁
     * 
     * @param waitTime 等待时间,单位毫秒
     * @return
     */
    boolean lock(long waitTime);

    /**
     * 解锁
     */
    void unlock();

}
