package org.jflame.toolkit.lock;

import java.util.concurrent.TimeUnit;

public class RedisLock implements DistributedLock {

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long waitTime, TimeUnit timeUnit) {
        return false;
    }

    @Override
    public void lock() {
    }

    @Override
    public void unlock() {
    }

}
