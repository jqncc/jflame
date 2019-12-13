package org.jflame.commons.lock;

/**
 * 分布式锁
 * 
 * @author yucan.zhang
 */
public interface DistributedLock {

    /**
     * 默认获取锁等待时间,单位毫秒100ms
     */
    int DEFAULT_WAIT_TIME = 100;

    String LOCK_KEY_PREFIX = "jf_dis_lock";

    /**
     * 获取锁
     * 
     * @param waitTime 等待时间,单位毫秒,获取锁的等待时间不应大于任务的执行时间
     * @return
     */
    boolean lock(long waitTime);

    /**
     * 解锁
     */
    void unlock();

}
