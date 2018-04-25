package org.jflame.toolkit.lock;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.util.StringHelper;

/**
 * zookeeper分布式共享锁实现
 * 
 * @author yucan.zhang
 */
public class ZookeeperLock implements DistributedLock, Closeable {

    private ZooKeeper zkclient;
    private final static String LOCK_ROOT_PATH = "/jflamezklock";
    private final static String LOCK_NODE_NAME = "mylock-";
    private String parentLockNode;
    private String currentLockNode;
    private String currentLockNodeName;

    /**
     * 构造函数
     * 
     * @param lockName 分布式锁名称
     * @param zkclient zookeeper实例
     * @throws Exception
     */
    public ZookeeperLock(String lockName, ZooKeeper zkclient) throws Exception {
        if (zkclient == null || StringHelper.isEmpty(lockName)) {
            throw new IllegalArgumentException("参数不为空");
        }
        this.zkclient = zkclient;
        createLockParentNode(lockName);
    }

    /**
     * 构造函数
     * 
     * @param lockName 分布式锁名称
     * @param connString zookeeper连接串
     */
    public ZookeeperLock(String lockName, String connString) throws Exception {
        if (StringHelper.isEmpty(connString) || StringHelper.isEmpty(lockName)) {
            throw new IllegalArgumentException("参数不为空");
        }
        int sessionTimeout = 5000;
        final CountDownLatch latch = new CountDownLatch(1);
        this.zkclient = new ZooKeeper(connString, sessionTimeout, new Watcher() {

            @Override
            public void process(WatchedEvent event) {
                if (event.getState().equals(KeeperState.SyncConnected)) {
                    latch.countDown();
                }
            }
        });
        latch.await();
        createLockParentNode(lockName);
    }

    private void createLockParentNode(String lockName) throws KeeperException, InterruptedException {
        parentLockNode = LOCK_ROOT_PATH + FileHelper.UNIX_SEPARATOR + lockName;
        if (zkclient.exists(LOCK_ROOT_PATH, false) == null) {
            zkclient.create(LOCK_ROOT_PATH, new byte[]{ 0 }, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        if (zkclient.exists(parentLockNode, false) == null) {
            zkclient.create(parentLockNode, new byte[]{ 1 }, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    @Override
    public synchronized boolean tryLock() {
        try {
            createLockNode();
            List<String> lockChilrens = zkclient.getChildren(currentLockNode, false);
            Collections.sort(lockChilrens);
            System.out.println(StringUtils.join(lockChilrens, ','));
            if (currentLockNodeName.equals(lockChilrens.get(0))) {
                return true;
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public synchronized boolean tryLock(long waitTime, TimeUnit timeUnit) {
        try {
            createLockNode();
            List<String> lockChilrens = zkclient.getChildren(currentLockNode, false);
            Collections.sort(lockChilrens);
            System.out.println(StringUtils.join(lockChilrens, ','));
            int myIndex = lockChilrens.indexOf(currentLockNodeName);
            if (myIndex == 0) {
                return true;
            } else if (myIndex > 0) {
                final String watchNode = parentLockNode + FileHelper.UNIX_SEPARATOR + lockChilrens.get(myIndex - 1);
                final CountDownLatch watchLatch = new CountDownLatch(1);
                Stat watchNodeStat = zkclient.exists(watchNode, new Watcher() {

                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getType().equals(EventType.NodeDeleted) && event.getPath().equals(watchNode)) {
                            watchLatch.countDown();
                        }
                    }
                });
                if (watchNodeStat == null) {
                    return true;// 前一节点已不存在,获取锁
                } else {
                    if (waitTime > 0) {
                        return watchLatch.await(waitTime, timeUnit);
                    } else {
                        watchLatch.await();
                        return true;
                    }
                }
            }
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException("error haven happen when get lock from zookeeper", e);
        }
        return false;
    }

    @Override
    public synchronized void lock() {
        tryLock(-1, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void unlock() {
        if (zkclient != null && zkclient.getState().isConnected()) {
            try {
                zkclient.delete(currentLockNode, -1);
            } catch (InterruptedException | KeeperException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (zkclient != null && zkclient.getState().isConnected()) {
            try {
                zkclient.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
                zkclient = null;
            }
        }
    }

    public void closeQuietly() {
        if (zkclient != null && zkclient.getState().isConnected()) {
            try {
                zkclient.close();
            } catch (Exception e) {
                e.printStackTrace();
                zkclient = null;
            }
        }
    }

    /**
     * 创建竞争者节点
     * 
     * @throws KeeperException
     * @throws InterruptedException
     */
    private void createLockNode() throws KeeperException, InterruptedException {
        currentLockNode = zkclient.create(parentLockNode + FileHelper.UNIX_SEPARATOR + LOCK_NODE_NAME, null,
                Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        currentLockNodeName = StringUtils.substringAfterLast(currentLockNode, "/");
    }

}
