package org.jflame.toolkit.test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.jflame.toolkit.lock.DistributedLock;
import org.jflame.toolkit.lock.ZookeeperLock;

public class LockTest {

    static int c = 0;

    public static void main(String[] args) {

        ExecutorService exeucotr = Executors.newCachedThreadPool();
        final int count = 400;
        final CountDownLatch latch = new CountDownLatch(count);
        final Set<Integer> set = new HashSet<>(100);
        final CountDownLatch latch1 = new CountDownLatch(1);
        ZooKeeper zkclient = null;
        try {
            zkclient = new ZooKeeper("127.0.0.1:2181", 2000, new Watcher() {

                @Override
                public void process(WatchedEvent event) {
                    if (event.getState().equals(KeeperState.SyncConnected)) {
                        latch1.countDown();
                    }
                }
            });
            latch1.await();

        } catch (IOException | InterruptedException e2) {
            e2.printStackTrace();
        }
        for (int i = 0; i < count; i++) {
            // final int t = i;
            try {
                final DistributedLock lock = new ZookeeperLock("testlock", zkclient);

                exeucotr.submit(new Runnable() {

                    public void run() {
                        try {
                            set.add(c++);
                            lock.lock(); // 获取锁
                            // System.out.println("thread " + t + ":" + (c++));
                        } finally {
                            latch.countDown();
                            lock.unlock();
                        }

                    }
                });
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(set.size());
        exeucotr.shutdown();
    }

}
