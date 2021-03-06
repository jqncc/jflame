package org.jflame.context.lock;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jflame.context.cache.redis.RedisAccessException;
import org.jflame.context.cache.redis.RedisClient;

/**
 * 基于redis的分布式锁(不可重入)实现
 * 
 * @author yucan.zhang
 */
public class RedisLock implements DistributedLock {

    private RedisClient redisClient;

    private volatile boolean locked = false;
    private static String UNLOCK_LUASCRIPT;

    private String lockKey;// 锁的键名
    private String lockValue;
    private int lockExpire;// 锁超时时间,单位秒

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("  return redis.call(\"del\",KEYS[1]) ");
        sb.append("else");
        sb.append("  return 0 ");
        sb.append("end");
        UNLOCK_LUASCRIPT = sb.toString();
    }

    public RedisLock(String lockName, int expireInSecond) {
        setLockKey(lockName);
        this.lockExpire = expireInSecond;
        if (lockExpire <= 0) {
            throw new IllegalArgumentException("锁的过期时间必须大于0");
        }
    }

    /**
     * 构造函数
     * 
     * @param _redisClient RedisClient
     * @param lockName 锁名,最终锁名"jf_dis_lock:lockName"
     * @param expireInSecond 锁超时时间,单位秒
     */
    public RedisLock(RedisClient _redisClient, String lockName, int expireInSecond) {
        this(lockName, expireInSecond);
        this.redisClient = _redisClient;
    }

    void setLockKey(String lockName) {
        this.lockKey = LOCK_KEY_PREFIX + ':' + lockName;
    }

    /**
     * 获取新锁. 执行过程:<br>
     * 1.通过setnx尝试设置某个key的值,成功(当前没有这个锁)则返回,成功获得锁 <br>
     * 2.锁已经存在则获取锁的到期时间,和当前时间比较,超时的话,则设置新的值
     * 
     * @param waitTime 获取锁等待时间，单位毫秒
     * @return 获取锁返回 true,已经锁定过或加锁失败返回false
     */
    public synchronized boolean lock(long waitTime) {
        if (waitTime < 1) {
            throw new IllegalArgumentException();
        }
        lockValue = UUID.randomUUID()
                .toString();
        long startTime = System.currentTimeMillis();
        long spanTime = 0;
        Lock localLock = new ReentrantLock();
        try {
            localLock.lock();
            do {
                if (setNX(lockKey, lockValue)) {
                    locked = true; // 上锁成功结束请求
                    return true;
                }
                if (spanTime > 10) {
                    try {
                        // 随机延迟
                        TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current()
                                .nextLong(spanTime));
                    } catch (InterruptedException e) {
                        // e.printStackTrace();
                    }
                }
                spanTime = System.currentTimeMillis() - startTime;
            } while (spanTime < waitTime);
        } finally {
            localLock.unlock();
        }
        return false;
    }

    /**
     * 释放锁
     */
    public void unlock() {
        if (locked) {
            List<String> keys = Arrays.asList(lockKey);
            List<String> values = Arrays.asList(lockValue);
            Long result = redisClient.runScript(UNLOCK_LUASCRIPT, keys, values, Long.class);
            locked = result == 0;
        }
    }

    private boolean setNX(final String key, final String value) {
        try {
            return redisClient.setIfAbsent(key, value, (long) getLockExpire(), TimeUnit.SECONDS);
        } catch (RedisAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getLockExpire() {
        return lockExpire;
    }

    public String getLockKey() {
        return lockKey;
    }

    public boolean isLocked() {
        return locked;
    }

    /* public String getLockValue() {
        return lockValue;
    }*/

    public void setRedisClient(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

}
