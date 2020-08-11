package org.jflame.context.lock;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.zookeeper.common.PathUtils;

import org.jflame.commons.model.Chars;
import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.util.UrlHelper;
import org.jflame.context.zookeeper.NodeDataListener;
import org.jflame.context.zookeeper.ZookeeperClient;
import org.jflame.context.zookeeper.curator.CuratorZookeeperClient;
import org.jflame.context.zookeeper.zkclient.ZkclientZookeeperClient;

/**
 * 基于zookeeper分布式锁(不可重入)实现
 * 
 * @author yucan.zhang
 */
public class ZookeeperLock implements DistributedLock {

    private final String SEQ_NODE_NAME_PRE = "mylock-";
    private String lockPath;// 锁的路径
    private int lockExpiryTime;// 锁超时时间,单位秒
    private final DistributedLock internalLock;

    public ZookeeperLock(ZookeeperClient _zkClient, String _lockName, int expireInSecond) {
        if (_zkClient == null) {
            throw new IllegalArgumentException("zkClient is not be null and connected");
        }
        lockExpiryTime = expireInSecond;
        this.lockPath = UrlHelper.mergeUrl(Chars.SLASH + LOCK_KEY_PREFIX, _lockName);
        PathUtils.validatePath(this.lockPath);
        if (_zkClient instanceof CuratorZookeeperClient) {
            internalLock = new CuratorLockImpl(((CuratorZookeeperClient) _zkClient).getClient());
        } else if (_zkClient instanceof ZkclientZookeeperClient) {
            internalLock = new ZkclientLockImpl(_zkClient);
        } else {
            throw new IllegalArgumentException(_zkClient.getClass() + " 未实现分布式锁");
        }
    }

    @Override
    public boolean lock(long waitTime) {
        Lock localLock = new ReentrantLock();
        try {
            localLock.lock();
            return internalLock.lock(waitTime);
        } finally {
            localLock.unlock();
        }
    }

    @Override
    public void unlock() {
        internalLock.unlock();
    }

    class ZkclientLockImpl implements DistributedLock {

        private String currentLockNode;// 生成的临时序列节点路径
        private String currentLockNodeName;
        private String watchNode = null;// 监听的前一节点

        private ZookeeperClient zkclient;

        public ZkclientLockImpl(ZookeeperClient zkclient) {
            this.zkclient = zkclient;
        }

        @Override
        public boolean lock(long waitTime) {
            boolean locked = false;
            long startMillisTime = System.currentTimeMillis();
            final String slash = "/";
            currentLockNode = zkclient.createEphemeral(lockPath + slash + SEQ_NODE_NAME_PRE, lockExpiryTime, true);
            currentLockNodeName = StringUtils.substringAfterLast(currentLockNode, slash);

            if (checkLock()) {
                locked = true;
            } else {
                // 监听前一个节点删除事件
                final CountDownLatch watchLatch = new CountDownLatch(1);
                NodeDataListener dataListener = new NodeDataListener() {

                    @Override
                    public void dataDeleted(String path) throws Exception {
                        watchLatch.countDown();
                    }

                    @Override
                    public void dataChange(String path, Object data) throws Exception {
                    }
                };
                try {
                    long overWaitTime = waitTime - (System.currentTimeMillis() - startMillisTime);
                    if (overWaitTime > 0) {
                        zkclient.registerDataListener(watchNode, dataListener);
                        if (watchLatch.await(overWaitTime, TimeUnit.MILLISECONDS)) {// watchLatch.await返回true表示执行了前面节点的删除事件
                            locked = checkLock();
                        }
                    }
                } catch (Exception e) {
                    locked = false;
                } finally {
                    zkclient.unregisterDataListener(watchNode, dataListener);
                }
            }
            return locked;
        }

        @Override
        public void unlock() {
            if (currentLockNode != null) {
                zkclient.delete(currentLockNode, false);
            }
        }

        private boolean checkLock() {
            List<String> lockChilrens = zkclient.getChildren(lockPath);
            sortChildren(lockChilrens);
            int myIndex = lockChilrens.indexOf(currentLockNodeName);
            if (myIndex == 0) {
                return true;
            } else {
                // 监听前一个节点删除事件
                if (watchNode == null) {
                    watchNode = lockPath + Chars.SLASH + lockChilrens.get(myIndex - 1);
                }
                return false;
            }
        }

        private void sortChildren(List<String> lockChilrens) {
            if (CollectionHelper.isNotEmpty(lockChilrens)) {
                lockChilrens.removeIf(l -> !l.startsWith(SEQ_NODE_NAME_PRE));
                Collections.sort(lockChilrens, new Comparator<String>() {

                    @Override
                    public int compare(String o1, String o2) {
                        String s1 = StringHelper.substringAfterIgnoreCase(o1, SEQ_NODE_NAME_PRE);
                        String s2 = StringHelper.substringAfterIgnoreCase(o2, SEQ_NODE_NAME_PRE);
                        return s1.compareTo(s2);
                    }
                });
            }
        }
    }

    class CuratorLockImpl implements DistributedLock {

        CuratorFramework curator;
        InterProcessLock lock;

        public CuratorLockImpl(CuratorFramework curator) {
            this.curator = curator;
            lock = new InterProcessSemaphoreMutex(curator, lockPath);
        }

        @Override
        public boolean lock(long waitTime) {
            try {
                return lock.acquire(waitTime, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void unlock() {
            if (lock != null) {
                try {
                    lock.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
