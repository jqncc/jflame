package org.jflame.toolkit.test;

import org.junit.Test;

import org.jflame.commons.zookeeper.ZookeeperClient;
import org.jflame.commons.zookeeper.zkclient.ZkclientZookeeperClient;

public class ZookeeperTest {

    private String connUrl = "127.0.0.1:2181";

    @Test
    public void testZkClient() {

        String node = "/abstest";
        String p = "/testa";
        String node1 = p + "/abs";

        ZookeeperClient client = null;

        try {
            client = new ZkclientZookeeperClient(connUrl);
            String newNodeName = client.createPersistent(node, true);
            System.out.println(newNodeName);
            boolean has = client.isExist(node);
            System.out.println(has);

            String node1path = client.createPersistent(node1, false);
            System.out.println("n1:" + node1path);

            boolean hasParent = client.isExist(p);
            System.out.println("hasParent:" + hasParent);

            client.delete(p, true);

            System.out.println(client.isExist(node1));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                client.close();
            }
        }

    }

    public static void main(String[] args) {
        // file:/D:/project/jflame/jflame/jflame-parent/jflame-toolkit/target/test-classes/org/jflame/toolkit/test/
        System.out.println(ZookeeperTest.class.getResource(""));
        // file:/D:/project/jflame/jflame/jflame-parent/jflame-toolkit/target/test-classes/
        System.out.println(ZookeeperTest.class.getClassLoader()
                .getResource(""));
        System.out.println(ZookeeperTest.class.getProtectionDomain()
                .getCodeSource()
                .getLocation());
    }

}
