package org.jflame.toolkit.key;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * 唯一字符生成工具类
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

}
