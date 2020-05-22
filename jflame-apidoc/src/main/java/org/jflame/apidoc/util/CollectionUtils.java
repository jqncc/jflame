package org.jflame.apidoc.util;

import java.util.Collection;

public final class CollectionUtils {

    /**
     * 判断集合是否为null或无元素
     * 
     * @param collection 集合
     * @param <E> 元素泛型
     * @return
     */
    public static <E> boolean isEmpty(Collection<E> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合不为null且至少有一个元素
     * 
     * @param collection 集合
     * @param <E> 元素泛型
     * @return
     */
    public static <E> boolean isNotEmpty(Collection<E> collection) {
        return collection != null && !collection.isEmpty();
    }

}
