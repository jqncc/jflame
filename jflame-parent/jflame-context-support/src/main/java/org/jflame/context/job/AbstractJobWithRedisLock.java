package org.jflame.context.job;

import org.jflame.toolkit.cache.RedisClient;
import org.jflame.toolkit.lock.DistributedLock;
import org.jflame.toolkit.lock.RedisLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJobWithRedisLock extends AbstractJobWithDistribtedLock {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private RedisClient redisClient;

    public AbstractJobWithRedisLock(String jobName) {
        this(jobName, 5);
    }

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
