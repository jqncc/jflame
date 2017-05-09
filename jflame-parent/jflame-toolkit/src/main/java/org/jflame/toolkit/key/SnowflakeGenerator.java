package org.jflame.toolkit.key;

import java.net.InetAddress;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.jflame.toolkit.codec.TranscodeHelper;
import org.jflame.toolkit.net.IPAddressHelper;

/**
 * snowflake唯一id生成算法实现.
 * <p>
 * 1位标识，最高位是符号位，正数是0，负数是1,id一般是正数，最高位是0<br />
 * +41位时间戳，（当前时间戳 - 开始时间戳）<br />
 * +N位的数据机器位,数据中心位+机器位<br />
 * +M位序列，1毫秒内最多生成M位序列<br />
 * <p><strong>注:单实例多线程安全,多实例可能产生重复id</strong>
 * @author yucan.zhang
 */
public final class SnowflakeGenerator {

    // 开始该类生成ID的时间戳
    private final static long startTime = 1483436297001L;
    // 机器id所占的位数
    private final static long workerIdBits = 8L;
    // 数据中心标识id所占的位数
    private final static long datacenterIdBits = 2L;
    // 支持的最大机器id =255
    private final static long maxWorkerId = -1L ^ (-1L << workerIdBits);
    // 支持的最大数据标识id=3
    private final static long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    // 序列在id中占的位数,每毫秒能生成的序列
    private final static long sequenceBits = 12L;
    // 机器id向左移的位数
    private final static long workerIdLeftShift = sequenceBits;
    // 数据标识id向左移的位数
    private final static long datacenterIdLeftShift = workerIdBits + workerIdLeftShift;
    // 时间戳向左移的位置
    private final static long timestampLeftShift = datacenterIdBits + datacenterIdLeftShift;
    // 生成序列的掩码
    private final static long sequenceMask = -1 ^ (-1 << sequenceBits);

    private long workerId;
    private long datacenterId;
    // 同一个时间戳内生成的序列数，初始值是0，从0开始
    private long sequence = 0L;
    // 上次生成id的时间戳
    private long lastTimestamp = -1L;
    private Random random = new Random();

    /**
     * 构造函数，默认workerid=ip%254，只适合各主机在同一局域网使用
     */
    public SnowflakeGenerator() {
        InetAddress ip = IPAddressHelper.getLocalIPAddress();
        if (ip != null) {
            long ipInt = TranscodeHelper.bytesToLong(ip.getAddress());
            this.workerId = ipInt % 254;
        } else {
            this.workerId = random.nextInt(254);
        }
        this.datacenterId = 1L;
    }

    /**
     * 构造函数
     * 
     * @param workerId 主机id
     * @param datacenterId 机房id
     */
    public SnowflakeGenerator(long workerId, long datacenterId) {
        if (workerId < 0 || workerId > maxWorkerId) {
            throw new IllegalArgumentException(String
                    .format("workerId[%d] is less than 0 or greater than maxWorkerId[%d].", workerId, maxWorkerId));
        }
        if (datacenterId < 0 || datacenterId > maxDatacenterId) {
            throw new IllegalArgumentException(
                    String.format("datacenterId[%d] is less than 0 or greater than maxDatacenterId[%d].", datacenterId,
                            maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 生成新id
     * 
     * @return
     */
    public synchronized long nextId() {
        long timestamp = curTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("时间设置错误，拒绝生成新的id %d 毫秒", lastTimestamp - timestamp));
        }
        // 表示是同一时间戳内生成的id
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 说明当前时间生成的序列数已达到最大，等待下一毫秒
            if (sequence == 0) {
                timestamp = utilNextMillis();
                sequence = randomInitSequence();
            }
        } else {
            sequence = randomInitSequence();
        }

        lastTimestamp = timestamp;

        return ((timestamp - startTime) << timestampLeftShift) // 时间戳部分
                | (datacenterId << datacenterIdLeftShift) // 数据标识id部分
                | (workerId << workerIdLeftShift) // 机器id部分
                | sequence; // 序列部分
    }

    /**
     * 初始值使用0-9随机，避免最后一位为0的数过多， 有利取模分表情况
     * 
     * @return
     */
    private long randomInitSequence() {
        return random.nextInt(9);
    }

    private long utilNextMillis() {
        long timestamp;
        do {
            timestamp = curTimeMillis();
        } while (timestamp <= lastTimestamp);
        return timestamp;
    }

    long curTimeMillis() {
        return System.currentTimeMillis();
    }

    // 测试
    public static void main(String[] args) {
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
        System.out.println(System.currentTimeMillis() - l);
        System.out.println(set.size());
        // for (Long long1 : set) { System.out.println(long1); }
    }

}