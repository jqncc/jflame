package org.jflame.toolkit.job;

import org.jflame.toolkit.lock.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用分布式锁的任务抽象类,保证一次只有一个点执行该任务
 */
public abstract class AbstractJobWithDistribtedLock {

    final protected Logger logger = LoggerFactory.getLogger(getClass());
    private int lockTimeout;
    protected String jobName;

    public AbstractJobWithDistribtedLock(String jobName) {
        this(jobName, 0);
    }

    /**
     * 构造函数
     * 
     * @param jobName 任务名称,同时做为分布式锁名,不可重复
     * @param lockTimeout 请求锁超时,单位秒.0表示不超时直到获取锁为止
     */
    public AbstractJobWithDistribtedLock(String jobName, int lockTimeout) {
        if (lockTimeout < 0) {
            lockTimeout = 0;
        }
        this.jobName = jobName;
    }

    /**
     * 运行任务
     */
    public void execute() {
        boolean isLock = false;
        logger.debug("job {} starting", jobName);
        DistributedLock lock = getLock(jobName);
        try {
            isLock = lock.lock(lockTimeout);
            if (isLock) {
                doExecute();
            } else {
                logger.error("job {} get lock failed", jobName);
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
     * @param jobName 任务名
     * @return
     */
    public abstract DistributedLock getLock(String jobName);

    /**
     * 实际业务执行
     * 
     * @return
     */
    public abstract void doExecute();

    public abstract String getLockPath();

}
