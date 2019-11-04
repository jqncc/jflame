package org.jflame.context.job;

import org.jflame.toolkit.cache.redis.RedisClient;
import org.jflame.toolkit.lock.DistributedLock;
import org.jflame.toolkit.lock.RedisLock;

/**
 * 简易任务基于redis分布式锁的实现
 * 
 * @author yucan.zhang
 */
public abstract class AbstractJobWithRedisLock extends AbstractJobWithDistribtedLock {

    private RedisClient redisClient;

    public AbstractJobWithRedisLock(String jobName, int lockTimeout) {
        super(jobName, lockTimeout);
    }

    @Override
    public DistributedLock getLock(String lockName) {
        return new RedisLock(getRedisClient(), lockName, getLockTimeout());
    }

    protected RedisClient getRedisClient() {
        return redisClient;
    }

    public void setRedisClient(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

}
