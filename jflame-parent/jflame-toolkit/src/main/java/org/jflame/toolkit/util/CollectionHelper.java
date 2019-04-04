package org.jflame.toolkit.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 集合工具
 * 
 * @see java.util.Collections
 * @author yucan.zhang
 */
public final class CollectionHelper {

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

    /**
     * 集合转数组
     * 
     * @param collection 枚举集合
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
     * 从集合中多个元素
     * 
     * @param collection 集合
     * @param removeElements 要删除的元素
     * @return
     */
    public static <E> boolean removeAll(Collection<E> collection, E[] removeElements) {
        if (collection == null || removeElements == null) {
            throw new NullPointerException();
        }
        return collection.removeIf(p -> ArrayUtils.contains(removeElements, p));
    }

    /**
     * 合并数组,并去重,元素顺序与原数组保持一至
     * 
     * @param arr1 数组1
     * @param arr2 数组2
     * @param <T> 泛型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] unionArray(T[] arr1, T[] arr2) {
        Set<T> newSet = new TreeSet<>();
        for (T t : arr1) {
            newSet.add(t);
        }
        for (T t : arr2) {
            newSet.add(t);
        }

        int len = newSet.size();
        int i = 0;

        final Class<?> type1 = arr1.getClass()
                .getComponentType();
        T[] joinedArray = (T[]) Array.newInstance(type1, len);

        for (T t : newSet) {
            joinedArray[i++] = t;
        }
        return joinedArray;
    }

    /**
     * 判断一个对象是否在一个集合迭代内
     * 
     * @param iterator 迭代器
     * @param element 要查找的对象
     * @return true存在
     */
    public static boolean contains(Iterator<?> iterator, Object element) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                Object candidate = iterator.next();
                if (Objects.equals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断一个对象是否在一个集合内
     * 
     * @param collection 集合
     * @param element 要查找的对象
     * @return true存在
     */
    public static <E> boolean contains(Collection<E> collection, E element) {
        for (E object : collection) {
            if (Objects.equals(object, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 集合用','拼接成字符串
     * 
     * @param collection 集合
     * @return string
     */
    public static <E> String toString(Collection<E> collection) {
        if (collection == null) {
            return null;
        }
        return StringUtils.join(collection, ',');
    }

    /**
     * 返回集合的最后一个元素,<b>传入无序集合时结果不确定</b>
     * 
     * @param collection Collection
     * @return 集合的最后一个元素
     */
    public static <T> T getLast(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        }

        // 当类型List时，直接取得最后一个元素.
        if (collection instanceof List) {
            List<T> list = (List<T>) collection;
            return list.get(list.size() - 1);
        }
        int i = 0,size = collection.size();
        for (T t : collection) {
            if (i == size - 1) {
                return t;
            }
            i++;
        }
        return null;
    }

    /**
     * 按条件过滤集合,返回符合条件的子集List
     * 
     * @param collection
     * @param predicate 条件
     * @return
     */
    public static <T> List<T> subList(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * 按条件过滤集合,返回符合条件的子集Set
     * 
     * @param collection
     * @param predicate 条件
     * @return
     */
    public static <T> Set<T> subSet(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return Collections.emptySet();
        }
        return collection.stream()
                .filter(predicate)
                .collect(Collectors.toSet());
    }
}
