package org.jflame.context.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.toolkit.lock.DistributedLock;

/**
 * 使用分布式锁的任务抽象类,保证一次只有一个点执行该任务
 */
public abstract class AbstractJobWithDistribtedLock {

    final protected Logger logger = LoggerFactory.getLogger(getClass());
    private int lockTimeout = 1;// 锁超时时间
    private int lockWaitTime = 200;// 获取锁等待时间
    protected String jobName;

    public AbstractJobWithDistribtedLock(String jobName) {
        this(jobName, 0);
    }

    /**
     * 构造函数
     * 
     * @param jobName 任务名称,同时做为分布式锁名,不可重复
     * @param lockTimeout 请求锁超时,单位秒.
     */
    public AbstractJobWithDistribtedLock(String jobName, int lockTimeout) {
        this.jobName = jobName;
    }

    /**
     * 运行任务
     */
    public void execute() {
        boolean isLock = false;
        logger.debug("job {} starting", jobName);
        String lockName = getClass().getPackage()
                .getName() + jobName;
        DistributedLock lock = getLock(lockName);
        try {
            isLock = lock.lock(lockWaitTime);
            if (isLock) {
                doExecute();
            } else {
                logger.error("job {} get lock failed", lockName);
            }
        } catch (Exception e) {
            logger.error(jobName + " error", e);
            throw e;
        } finally {
            if (isLock && lock != null) {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    logger.error("job {} release lock failed", jobName);
                }
            }
        }
    }

    /**
     * 获取一个分布式锁
     * 
     * @param lockName 锁名
     * @return
     */
    public abstract DistributedLock getLock(String lockName);

    /**
     * 实际业务执行
     */
    public abstract void doExecute();

    public void setLockTimeout(int lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    public int getLockWaitTime() {
        return lockWaitTime;
    }

    public void setLockWaitTime(int lockWaitTime) {
        this.lockWaitTime = lockWaitTime;
    }

    public int getLockTimeout() {
        return lockTimeout;
    }

}
