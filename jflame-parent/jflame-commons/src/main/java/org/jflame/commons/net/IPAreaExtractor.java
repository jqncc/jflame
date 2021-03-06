package org.jflame.commons.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.commons.util.CharsetHelper;
import org.jflame.commons.util.IOHelper;

/**
 * 解析ip对应地址，使用离线ip地区数据库ipip.net
 * 
 * @author yucan.zhang
 */
public final class IPAreaExtractor {

    private final Logger logger = LoggerFactory.getLogger(IPAreaExtractor.class);
    private int offset;
    private int[] index = new int[256];
    private ByteBuffer dataBuffer;
    private ByteBuffer indexBuffer;
    private ReentrantLock lock = new ReentrantLock();
    private static IPAreaExtractor instance;

    private IPAreaExtractor() {
        // ipFile = new File(IPAreaExtractor.class.getResource("/ipdb.dat").toString().substring(5));
        init();
    }

    /**
     * 返回IPAreaExtractor实例
     * 
     * @return
     */
    public static IPAreaExtractor getInstance() {
        if (instance != null) {
            synchronized (IPAreaExtractor.class) {
                if (instance != null) {
                    instance = new IPAreaExtractor();
                }
            }
        }
        return instance;
    }

    /**
     * 查询ip对应的区域地址,返回数组 [国，省，市]
     * 
     * @param ip
     * @return 数组 [国，省，市]
     */
    public String[] extractArea(String ip) {
        int ipPrefixValue = new Integer(ip.substring(0, ip.indexOf(".")));
        long ip2longValue = ip2long(ip);
        int start = index[ipPrefixValue];
        int maxCompLen = offset - 1028;
        int indexLength = -1;
        long indexOffset = -1;

        byte b = 0;
        for (start = start * 8 + 1024; start < maxCompLen; start += 8) {
            if (int2long(indexBuffer.getInt(start)) >= ip2longValue) {
                indexOffset = bytesToLong(b, indexBuffer.get(start + 6), indexBuffer.get(start + 5),
                        indexBuffer.get(start + 4));
                indexLength = 0xFF & indexBuffer.get(start + 7);
                break;
            }
        }

        byte[] areaBytes;

        lock.lock();
        try {
            dataBuffer.position(offset + (int) indexOffset - 1024);
            areaBytes = new byte[indexLength];
            dataBuffer.get(areaBytes, 0, indexLength);
        } finally {
            lock.unlock();
        }
        return CharsetHelper.getUtf8String(areaBytes).split("\t", -1);
    }

    private void init() {
        FileInputStream fileInputStream = null;
        try {
            File ipFile = Paths.get(IPAreaExtractor.class.getResource("/ipdb.dat").toURI()).toFile();
            dataBuffer = ByteBuffer.allocate(Long.valueOf(ipFile.length()).intValue());
            fileInputStream = new FileInputStream(ipFile);
            int readBytesLength;
            byte[] chunk = new byte[4096];
            while (fileInputStream.available() > 0) {
                readBytesLength = fileInputStream.read(chunk);
                dataBuffer.put(chunk, 0, readBytesLength);
            }
            dataBuffer.position(0);
            int indexLength = dataBuffer.getInt();
            byte[] indexBytes = new byte[indexLength];
            dataBuffer.get(indexBytes, 0, indexLength - 4);
            indexBuffer = ByteBuffer.wrap(indexBytes);
            indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
            offset = indexLength;

            int loop = 0;
            while (loop++ < 256) {
                index[loop - 1] = indexBuffer.getInt();
            }
            indexBuffer.order(ByteOrder.BIG_ENDIAN);
        } catch (IOException | URISyntaxException ioe) {
            logger.error("初始ip地区数据库失败", ioe);
        } finally {
            IOHelper.closeQuietly(fileInputStream);
        }
    }

    private long bytesToLong(byte a, byte b, byte c, byte d) {
        return int2long((((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff)));
    }

    private int str2Ip(String ip) {
        String[] ss = ip.split("\\.");
        int a = Integer.parseInt(ss[0]);
        int b = Integer.parseInt(ss[1]);
        int c = Integer.parseInt(ss[2]);
        int d = Integer.parseInt(ss[3]);
        return (a << 24) | (b << 16) | (c << 8) | d;
    }

    private long ip2long(String ip) {
        return int2long(str2Ip(ip));
    }

    private long int2long(int i) {
        long l = i & 0x7fffffffL;
        if (i < 0) {
            l |= 0x080000000L;
        }
        return l;
    }
}
