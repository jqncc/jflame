package org.jflame.commons.lock;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.common.PathUtils;

import org.jflame.commons.common.Chars;
import org.jflame.commons.util.UrlHelper;
import org.jflame.commons.zookeeper.NodeDataListener;
import org.jflame.commons.zookeeper.ZookeeperClient;
import org.jflame.commons.zookeeper.curator.CuratorZookeeperClient;
import org.jflame.commons.zookeeper.zkclient.ZkclientZookeeperClient;

/**
 * 基于zookeeper(Curator库)的分布式锁(不可重入)实现
 * 
 * @author yucan.zhang
 */
public class ZookeeperLock implements DistributedLock {

    private String lockKey;// 锁的键名

    private DistributedLock internalLock;

    public ZookeeperLock(ZookeeperClient zkClient, String lockName) {
        setLockKey(lockName);
        if (!zkClient.isExist(lockKey)) {
            zkClient.createPersistent(lockKey, false);
        }
        if (zkClient instanceof ZkclientZookeeperClient) {
            internalLock = new ZkclientLock((ZkclientZookeeperClient) zkClient, lockKey);
        } else {
            internalLock = new CuratorLock((CuratorZookeeperClient) zkClient, lockKey);
        }
    }

    @Override
    public boolean lock(long waitTime) {
        if (waitTime < 1) {
            waitTime = DEFAULT_WAIT_TIME;
        }
        return internalLock.lock(waitTime);
    }

    @Override
    public void unlock() {
        if (internalLock != null) {
            internalLock.unlock();
        }
    }

    public void setLockKey(String lockName) {
        this.lockKey = UrlHelper.mergeUrl(Chars.SLASH + LOCK_KEY_PREFIX, lockName);
        PathUtils.validatePath(this.lockKey);
    }

    private final class CuratorLock implements DistributedLock {

        private CuratorFramework zkClient;
        private InterProcessSemaphoreMutex mutex = null;
        private String lockPath;

        public CuratorLock(CuratorZookeeperClient curatorClient, String lockPath) {
            zkClient = curatorClient.getClient();
            this.lockPath = lockPath;
        }

        @Override
        public boolean lock(long waitTime) {
            try {
                mutex = new InterProcessSemaphoreMutex(zkClient, lockPath);
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

    private final class ZkclientLock implements DistributedLock {

        private final String lockSeqNamePrefix = "mylock-";
        private ZkclientZookeeperClient zkclient;
        private String currentLockNode;
        private String currentLockNodeName;
        private String lockPath;

        /**
         * 构造函数
         * 
         * @param lockName 分布式锁名称
         * @param zkclient zookeeper实例
         * @throws Exception
         */
        public ZkclientLock(ZkclientZookeeperClient zkclient, String lockPath) {
            this.zkclient = zkclient;
            this.lockPath = lockPath;
        }

        @Override
        public synchronized boolean lock(long waitTime) {
            boolean locked = false;
            long startMillisTime = System.currentTimeMillis();

            createLockNode();
            List<String> lockChilrens = zkclient.getChildren(currentLockNode);
            Collections.sort(lockChilrens);// 名称前面一样,只是后面序号不同,直接比较
            int myIndex = lockChilrens.indexOf(currentLockNodeName);
            if (myIndex == 0) {
                locked = true;
            } else if (myIndex > 0) {
                final String watchNode = lockKey + Chars.SLASH + lockChilrens.get(myIndex - 1);
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
                    zkclient.registerDataListener(watchNode, dataListener);
                    long overWaitTime = waitTime - System.currentTimeMillis() - startMillisTime;
                    if (overWaitTime > 0) {
                        locked = watchLatch.await(waitTime, TimeUnit.MILLISECONDS);// watchLatch.await返回true表示执行了前面节点的删除事件
                    }
                } catch (Exception e) {
                    locked = false;
                } finally {
                    zkclient.unregisterDataListener(watchNode, dataListener);
                }
            }
            // 没拿到锁,删除新建的节点
            if (!locked) {
                zkclient.delete(currentLockNode, false);
            }
            return locked;
        }

        public void unlock() {
            if (currentLockNode != null) {
                zkclient.delete(currentLockNode, false);
            }
        }

        /**
         * 创建锁节点(临时有序节点)
         * 
         * @throws KeeperException
         * @throws InterruptedException
         */
        private void createLockNode() {
            currentLockNode = zkclient.createEphemeral(lockPath + Chars.SLASH + lockSeqNamePrefix, true);
            currentLockNodeName = StringUtils.substringAfterLast(currentLockNode, "/");
        }

    }

    /*  public static void main(String[] args) {
        List<String> lockChilrens = Arrays.asList("test-0002", "test-0001", "test-0011");
        Collections.sort(lockChilrens, new Comparator<String>() {
    
            @Override
            public int compare(String o1, String o2) {
                String split = "-";
                String oo1 = StringUtils.substringAfterLast(o1, split);
                String oo2 = StringUtils.substringAfterLast(o2, split);
                return oo1.compareTo(oo2);
            }
        });
        System.out.println(lockChilrens);
    }*/
}
