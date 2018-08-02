package org.jflame.toolkit.test;

import org.jflame.toolkit.zookeeper.ZookeeperClient;
import org.jflame.toolkit.zookeeper.curator.CuratorZookeeperClient;
import org.jflame.toolkit.zookeeper.zkclient.ZkclientZookeeperClient;
import org.junit.Test;

public class ZookeeperTest {

    private String connUrl = "127.0.0.1:2181";

    @Test
    public void test() {

        String node = "/abs";
        ZookeeperClient client = new ZkclientZookeeperClient(connUrl);
        client.create(node);
        boolean has = client.isExist(node);
        System.out.println(has);

        ZookeeperClient clientCur = new CuratorZookeeperClient(connUrl);
        boolean hasCur = clientCur.isExist(node);
        System.out.println(hasCur);

        clientCur.delete(node);

        System.out.println(client.isExist(node));
        client.close();
        clientCur.close();
    }

}
