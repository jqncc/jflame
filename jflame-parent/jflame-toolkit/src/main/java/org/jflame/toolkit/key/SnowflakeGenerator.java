package org.jflame.toolkit.key;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import org.jflame.toolkit.util.StringHelper;

/**
 * snowflake唯一id生成算法实现.
 * <p>
 * 1位标识，最高位是符号位，正数是0，负数是1,id一般是正数，最高位是0<br>
 * +41位时间戳，（当前时间戳 - 开始时间戳）<br>
 * +N位的数据机器位,数据中心位+机器位<br>
 * +M位序列，1毫秒内最多生成M位序列<br>
 * <p>
 * <strong>注:单实例多线程安全</strong>
 * 
 * @author yucan.zhang
 */
public final class SnowflakeGenerator {

    // 开始该类生成ID的时间戳
    private final static long startTime = 1483436297001L;
    // 机器id所占的位数
    private final static long workerIdBits = 5L;
    // 数据中心标识id所占的位数
    private final static long datacenterIdBits = 5L;
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
    private ThreadLocalRandom random = ThreadLocalRandom.current();

    /**
     * 构造函数,默认使用机器MAC地址和进程号作为数据中心位和机器位
     */
    public SnowflakeGenerator() {
        this.datacenterId = getDatacenterId(maxDatacenterId);
        this.workerId = getMaxWorkerId(datacenterId, maxWorkerId);
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
     * 生成新id,返回16进制字符串
     * 
     * @return 新id的16进制表示
     */
    public synchronized String nextHexId() {
        return Long.toHexString(nextId());
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

    /**
     * <p>
     * 获取 maxWorkerId
     * </p>
     */
    protected static long getMaxWorkerId(long datacenterId, long maxWorkerId) {
        StringBuilder mpid = new StringBuilder();
        mpid.append(datacenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (StringHelper.isNotEmpty(name)) {
            /*
             * GET jvmPid
             */
            mpid.append(name.split("@")[0]);
        }
        /*
         * MAC + PID 的 hashcode 获取16个低位
         */
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }

    /**
     * <p>
     * 数据标识id部分
     * </p>
     */
    protected static long getDatacenterId(long maxDatacenterId) {
        long id = 0L;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                id = 1L;
            } else {
                byte[] mac = network.getHardwareAddress();
                if (null != mac) {
                    id = ((0x000000FF & (long) mac[mac.length - 1])
                            | (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
                    id = id % (maxDatacenterId + 1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("获取数据中心标识异常");
        }
        return id;
    }

    public static void main(String[] args) {
        // SnowflakeGenerator generator = new SnowflakeGenerator();
        // System.out.println(generator.nextId());
        System.out.println(new Date(1483436297001L));
    }
    // 测试
    // public static void main(String[] args) {
    // final int threadCount = 3;
    // final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
    // final Set<Long> set = new java.util.concurrent.CopyOnWriteArraySet<>();
    //
    // final SnowflakeGenerator idWorker = new SnowflakeGenerator(0, 1);
    //
    // long l = System.currentTimeMillis();
    // for (int i = 0; i < threadCount; i++) {
    // new Thread(new Runnable() {
    //
    // @Override
    // public void run() {
    // int j = 0;
    // Long tmp;
    // while (j < 1000) {
    // tmp = idWorker.nextId();
    // set.add(tmp);
    // System.out.println(tmp);
    // j++;
    // }
    // countDownLatch.countDown();
    // }
    // }).start();
    // }
    // try {
    // countDownLatch.await();
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // System.out.println(System.currentTimeMillis() - l);
    // System.out.println(set.size());
    // // for (Long long1 : set) { System.out.println(long1); }
    // }

}