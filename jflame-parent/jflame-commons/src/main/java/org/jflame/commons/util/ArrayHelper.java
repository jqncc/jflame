package org.jflame.commons.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * 数组工具类
 * 
 * @author yucan.zhang
 */
public final class ArrayHelper {

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
     * 判断数组不为null且至少有一个元素
     * 
     * @param array
     * @return
     */
    public static <T> boolean isNotEmpty(T[] array) {
        return array != null && array.length > 0;
    }

    /**
     * 合并数组,并去重,元素顺序与原数组保持一至
     * 
     * @param arr1 数组1
     * @param arr2 数组2
     * @param <T> 泛型
     * @return
     */
    public static <T> T[] unionArray(T[] arr1, T[] arr2) {
        Set<T> newSet = new TreeSet<>();
        for (T t : arr1) {
            newSet.add(t);
        }
        for (T t : arr2) {
            newSet.add(t);
        }
        return toArray(newSet);
    }

    /**
     * 提取集合中的某属性转为字符串数组
     * 
     * @param collection 集合
     * @param mapper 提取的属性stream操作Function
     * @return 集合某属性的字符串数组
     */
    public static <T,R> String[] toArray(Collection<T> collection, Function<? super T,? extends R> mapper) {
        if (CollectionHelper.isEmpty(collection)) {
            throw new IllegalArgumentException("parameter 'collection' not be null");
        }
        return collection.stream()
                .map(mapper)
                .toArray(String[]::new);
    }

    /**
     * 集合转数组
     * 
     * @param collection
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] toArray(Collection<E> collection) {
        E[] arr = (E[]) Array.newInstance(collection.iterator()
                .next()
                .getClass(), collection.size());
        return collection.toArray(arr);
    }

    /**
     * 判断两个数组元素是否相同,如果两个数组长度相同,任意一个元素都可以在另一数组中找到相同的元素(元素顺序可以不一致)即认为数组是相同的.
     * 
     * @param array1 数组1
     * @param array2 数组2
     * @return 如果元素相同返回true
     */
    public static <T> boolean elementEquals(T[] array1, T[] array2) {
        if (array1 == null) {
            return array2 == null;
        } else {
            if (array2 == null) {
                return false;
            }
            // 元素个数不相等
            if (array1.length != array2.length) {
                return false;
            }
            boolean equals = true;
            for (T a1 : array1) {
                if (!Arrays.stream(array2)
                        .anyMatch(a2 -> Objects.equals(a1, a2))) {
                    equals = false;
                    break;
                }
            }
            return equals;
        }
    }

    /**
     * 判断数组array2是否是数组array1的子集
     * 
     * @param array1
     * @param array2
     * @return
     */
    public static <T> boolean isSub(T[] array1, T[] array2) {
        if (array1.length < array2.length) {
            return false;
        }
        for (T a1 : array1) {
            for (T a2 : array2) {
                if (!Objects.equals(a1, a2)) {
                    return false;
                }
            }
        }
        return true;
    }

}
