package org.jflame.toolkit.test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.jflame.toolkit.key.ObjectId;
import org.jflame.toolkit.key.SnowflakeGenerator;
import org.junit.Test;

public class IdGenTest {

    @Test
    public void testSnowGen() {
        final int threadCount = 3;
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        final Set<Long> set = new java.util.concurrent.CopyOnWriteArraySet<>();

        final SnowflakeGenerator idWorker = new SnowflakeGenerator(0, 1);

        long l = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    int j = 0;
                    Long tmp;
                    while (j < 1000) {
                        tmp = idWorker.nextId();
                        set.add(tmp);
                        System.out.println(tmp);
                        j++;
                    }
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(idWorker.nextHexId());
        System.out.println(System.currentTimeMillis() - l);
        System.out.println(set.size());
        // for (Long long1 : set) { System.out.println(long1); }
    }

    @Test
    public void testObjID() {
        ObjectId oId = new ObjectId();
        System.out.println(oId.toString());
        System.out.println(oId.toHexString());
        ObjectId oId1 = new ObjectId();
        System.out.println(oId1.toHexString());
    }
}
