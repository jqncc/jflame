package org.jflame.test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import org.jflame.context.lock.ZookeeperLock;
import org.jflame.context.zookeeper.ChildNodeListener;
import org.jflame.context.zookeeper.NodeDataListener;
import org.jflame.context.zookeeper.ZookeeperClient;
import org.jflame.context.zookeeper.zkclient.ZkclientZookeeperClient;

public class ZookeeperTest {

    private String connUrl = "127.0.0.1:2181";
    ZookeeperClient client = null;

    @Before
    public void init() {
        client = new ZkclientZookeeperClient(connUrl);
        // client = new CuratorZookeeperClient(connUrl);
    }

    @Test
    public void testZkOpt2() {

        System.out.println(client.createEphemeral("/c/m_", true));
        client.close();
    }

    @Test
    public void testZkOpt() {

        String node = "/abstest";
        String p = "/testa";
        String node1 = p + "/abs";

        try {
            System.out.println(client.createPersistent("/b", false));
            String newNodeName = client.createPersistent(node, true);
            boolean has = client.isExist(newNodeName);
            System.out.println("创建顺序节点:" + newNodeName + " =" + has);

            String node1path = client.createPersistent(node1, false);
            System.out.println("创建节点:" + node1path);

            boolean hasParent = client.isExist(p);
            System.out.println("父节点同时创建" + p + ":" + hasParent);

            client.writeDate(node1path, "nodetext" + RandomUtils.nextInt());
            String nodeData = client.readData(node1path);
            System.out.println("修改节点内容," + node1path + " = " + nodeData);
            Stat stat = new Stat();
            client.readData(node1path, stat);
            System.out.println("stat:" + stat);
            client.delete(p, true);
            System.out.println("递归删除:" + !client.isExist(node1));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                client.close();
            }
        }

    }

    @Test
    public void testChildEvent() {
        client.registerChildListener("/a", new ChildNodeListener() {

            @Override
            public void childChanged(String path, List<String> children) {
                System.out.println("节点修改事件:" + path + " 当前子节点:" + children);
            }
        });
        String newnode = "/a/new2";
        client.createPersistent(newnode, false);// 创建直接子节点触发事件

        client.writeDate(newnode, "xp");// 修改直接子子节点触发事件
        client.createPersistent(newnode + "/nn1", false);// 增加子孙子节点不触发事件
        client.delete(newnode, true);// 删除子子节点触发事件

        try {
            System.out.println("==waiting===");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNodeDataEvent() {
        String watchNode = "/a/new1";
        if (!client.isExist(watchNode)) {
            client.createPersistent(watchNode, "newcont", false);
        }
        // 注册节点数据监听, 节点不存在不会报错,节点创建后仍会触发相关事件
        NodeDataListener dataListener = new NodeDataListener() {

            @Override
            public void dataDeleted(String path) throws Exception {
                System.out.println("节点删除:" + path);
            }

            @Override
            public void dataChange(String path, Object data) throws Exception {
                System.out.println("节点数据修改:" + path + " :" + data);
            }
        };
        client.registerDataListener(watchNode, dataListener);
        /*
         * zkclient 先注册事件,再创建节点,创建节点时会触发数据修改事件
         * 
        if (!client.isExist(watchNode)) {
            client.createPersistent(watchNode, false);
        }*/
        // zkclient先修改数据,再马上删除节点,数据修改节点不会触发,但会触发两次删除事件.原因是在数据修改事件中如果发现节点不存在将不会再执行转而自动触发一次删除事件
        client.writeDate(watchNode, "newcontxpccc");
        try {
            TimeUnit.MILLISECONDS.sleep(100L);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        client.unregisterDataListener(watchNode, dataListener);

        client.writeDate(watchNode, "中国字");
        String zh = client.readData(watchNode);
        System.out.println(zh);
        client.delete(watchNode, false);

        try {
            System.out.println("==waiting===");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLock() {
        final int threadCount = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    ZookeeperLock lock = null;
                    try {
                        lock = new ZookeeperLock(client, "test-lock", 10);
                        if (lock.lock(1000)) {
                            System.out.println("get locked threadId:" + Thread.currentThread()
                                    .getId());
                            countDownLatch.countDown();
                            /* try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                        } else {
                            System.out.println("lock failed threadId:" + Thread.currentThread()
                                    .getId());
                        }
                    } finally {
                        lock.unlock();
                    }

                }
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
