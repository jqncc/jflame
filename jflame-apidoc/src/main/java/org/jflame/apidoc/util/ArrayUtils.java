package org.jflame.apidoc.util;

import java.util.Objects;

/**
 * 数组工具类
 *
 * @author yucan.zhang
 */
public class ArrayUtils {

    /**
     * 判断数组是否为null或无元素
     * 
     * @param array
     * @return
     */
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断数组不为null且有元素
     * 
     * @param array
     * @return
     */
    public static <T> boolean isNotEmpty(T[] array) {
        return array != null && array.length > 0;
    }

    /**
     * 判断数组中是否包含某元素
     * 
     * @param array
     * @param findValue
     * @return
     */
    public static <T> boolean contains(Object[] array, Object findValue) {
        if (isEmpty(array)) {
            return false;
        }
        for (Object obj : array) {
            if (Objects.equals(obj, findValue)) {
                return true;
            }
        }
        return false;
    }
}
