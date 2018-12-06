package org.jflame.toolkit.lock;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.jflame.toolkit.util.UrlHelper;
import org.jflame.toolkit.zookeeper.curator.CuratorZookeeperClient;

public class ZookeeperLock implements DistributedLock {

    private final int DEFAULT_WAIT_TIME = 5 * 1000;// 默认获取锁等待时间5秒
    private final String lockKeyPrefix = "/zklock/";

    private String lockKey;// 锁的键名
    private CuratorFramework zkClient;
    private InterProcessSemaphoreMutex mutex = null;

    public ZookeeperLock(CuratorZookeeperClient zkClient, String lockName) {
        super();
        this.lockKey = UrlHelper.mergeUrl(lockKeyPrefix, lockName);
        this.zkClient = zkClient.getClient();
    }

    @Override
    public boolean lock(long waitTime) {
        if (waitTime < 0) {
            waitTime = DEFAULT_WAIT_TIME;
        }
        try {
            mutex = new InterProcessSemaphoreMutex(zkClient, lockKey);
            return mutex.acquire(waitTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void unlock() {
        if (mutex != null) {
            try {
                mutex.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
