package org.jflame.context.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.commons.lock.DistributedLock;

/**
 * 一个简易的分布式任务调试方案抽象类,使用分布式锁的任务,保证一次只有一个点执行该任务.
 * <ol>
 * <li>同一时间成功获取分布式锁的终端执行任务,任务完成后解锁.由于各终端时间并非完全一致所以默认各终端的任务仍可能会执行,但不会在同一个时间点并发执行.</li>
 * <li>如果要求一个任务周期内一个终端执行任务后其他终端不再执行,<strong>可将属性oneNodeMode设置为true,并将锁超时时间设为任务周期时间. </strong><br>
 * 具体实现: 先获取执行权的任务执行完成后并不解除锁,锁过期自动解除(实现类的分布式锁必须是可以过期自动解除的),即一个任务获取锁后直到下一任务执行时间点前只有一个终端执行该任务.
 * <li>如果执行过程发生异常,不管oneNodeMode是否等于true都会立即解锁</li>
 * <li>获取锁的等待时间应小于任务执行时间,默认200ms</li>
 * <li>锁的过期时间应小于任务执行周期</li>
 * <li>此方案较为粗糙,适用于任务周期较大,且任务重复执行影响不大的情况</li>
 * </ol>
 */
public abstract class AbstractJobWithDistribtedLock {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private int lockTimeout;// 锁超时时间
    private int lockWaitTime = 200;// 获取锁等待时间
    protected String jobName;
    private boolean oneNodeMode = false;// 一个任务周期内只允许一个任务运行

    /**
     * 构造函数
     * 
     * @param jobName 任务名称,同时做为分布式锁名,不可重复
     * @param lockTimeout 请求锁超时,单位秒.
     */
    public AbstractJobWithDistribtedLock(String jobName, int lockTimeout) {
        this.jobName = jobName;
        this.lockTimeout = lockTimeout;
    }

    /**
     * 运行任务
     */
    public void execute() {
        boolean isLock = false;
        boolean isImmediatelyUnLock = !oneNodeMode;
        if (logger.isDebugEnabled()) {
            logger.debug("任务 {}开始", jobName);
        }
        String lockName = getClass().getPackage()
                .getName() + jobName;
        DistributedLock lock = getLock(lockName);
        try {
            isLock = lock.lock(getLockTimeout());
            if (isLock) {
                doExecute();
            } else {
                logger.error("任务 {} 获取锁失败未能执行", lockName);
            }
        } catch (Exception e) {
            logger.error("任务执行中异常" + jobName, e);
            isImmediatelyUnLock = true;
            throw e;
        } finally {
            if (isImmediatelyUnLock && isLock && lock != null) {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    logger.error("任务 {}释放锁失败", jobName);
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

    public boolean isOneNodeMode() {
        return oneNodeMode;
    }

    public void setOneNodeMode(boolean oneNodeMode) {
        this.oneNodeMode = oneNodeMode;
    }

}
