package org.jflame.context.spring.lock;

import org.jflame.commons.cache.redis.RedisClient;
import org.jflame.commons.lock.RedisLock;

public abstract class LockTemplate {

    void execute(RedisClient client) {
        RedisLock lock = new RedisLock(client, "", 5);
        try {
            if (lock.lock(4)) {
                doExecute();
            }
        } finally {
            lock.unlock();
        }
    }

    abstract void doExecute();
}
