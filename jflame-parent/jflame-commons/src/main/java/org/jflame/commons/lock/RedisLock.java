package org.jflame.commons.lock;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jflame.commons.cache.redis.RedisClient;

/**
 * redis分布式锁
 * 
 * @author yucan.zhang
 */
public class RedisLock implements DistributedLock {

    private RedisClient redisClient;

    private final int DEFAULT_WAIT_TIME = 200;// 默认获取锁等待时间100ms
    private volatile boolean locked = false;
    private final static String lockKeyPrefix = "redis:lock:";
    private static String UNLOCK_LUASCRIPT;

    private String lockKey;// 锁的键名
    private String lockValue;
    private long lockExpire;// 锁超时时间,单位秒

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

    /**
     * 构造函数
     * 
     * @param _redisClient RedisClient
     * @param lockName 锁名,最终锁名"redis:lock:lockName"
     * @param expireInSecond 锁超时时间,单位秒
     */
    public RedisLock(RedisClient _redisClient, String lockName, long expireInSecond) {
        this.redisClient = _redisClient;
        this.lockKey = lockKeyPrefix + lockName;
        this.lockExpire = expireInSecond;
        if (lockExpire <= 0) {
            throw new IllegalArgumentException("锁的过期时间必须大于0");
        }
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
            waitTime = DEFAULT_WAIT_TIME;
        }
        Random random = new Random();
        lockValue = UUID.randomUUID()
                .toString();
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < waitTime) {
            if (setNX(lockKey, lockValue)) {
                locked = true; // 上锁成功结束请求
                return true;
            }
            /* 随机延迟 */
            try {
                Thread.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 释放锁
     */
    public synchronized void unlock() {
        if (locked) {
            List<String> keys = Arrays.asList(lockKey);
            List<String> values = Arrays.asList(lockValue);
            Long result = redisClient.runSHAScript(UNLOCK_LUASCRIPT, keys, values, Long.class);
            locked = result == 0;
        }
    }

    private boolean setNX(final String key, final String value) {
        return redisClient.setIfAbsent(key, value, lockExpire, TimeUnit.SECONDS);
    }

    public long getLockExpire() {
        return lockExpire;
    }

    public String getLockKey() {
        return lockKey;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getLockValue() {
        return lockValue;
    }

}