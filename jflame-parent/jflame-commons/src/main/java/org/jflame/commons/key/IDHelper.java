package org.jflame.commons.key;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 常用唯一键生成工具类
 * 
 * @author yucan.zhang
 */
public final class IDHelper {

    /**
     * 生成一个ObjectID
     * 
     * @return
     */
    public static String objectId() {
        return ObjectId.get()
                .toString();
    }

    /**
     * 生成一个uuid字符串(不带-)
     * 
     * @return
     */
    public static String uuid() {
        return StringUtils.remove(UUID.randomUUID()
                .toString(), '-');
    }

    /**
     * 生成一个 当前时间戳+随机数字 组合的字符串
     * 
     * @param randomCount 生成随机数位数
     * @return
     */
    public static String millisAndRandom(int randomCount) {
        return System.currentTimeMillis() + RandomStringUtils.randomNumeric(randomCount);
    }
}
