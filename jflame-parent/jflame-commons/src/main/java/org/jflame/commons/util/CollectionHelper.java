package org.jflame.commons.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.jflame.commons.model.Chars;

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
     * @deprecated 请使用ArrayHelper里的同名方法
     * @param collection 枚举集合
     * @return
     */
    @SuppressWarnings("unchecked")
    @Deprecated
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
        return StringUtils.join(collection, Chars.COMMA);
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
     * @param collection 集合
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
     * @param collection 集合
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

    /**
     * 用给定元素生成一个List,List实现为ArrayList
     * 
     * @param elements
     * @return
     */
    @SafeVarargs
    public static <T> List<T> newList(T... elements) {
        if (elements == null) {
            return null;
        }
        ArrayList<T> lst = new ArrayList<>(elements.length);
        Collections.addAll(lst, elements);
        return lst;
    }

    /**
     * 用给定元素生成一个Set,Set实现为HashSet
     * 
     * @param elements
     * @return
     */
    @SafeVarargs
    public static <T> Set<T> newSet(T... elements) {
        if (elements == null) {
            return null;
        }
        Set<T> set = new HashSet<>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    /**
     * 尝试克隆一个集合.通过序列化方式克隆对象
     * 
     * @param src 要克隆的集合
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> deepClone(Collection<T> src) {
        Collection<T> dest = null;
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            dest = (Collection<T>) in.readObject();
        } catch (IOException e) {

        } catch (ClassNotFoundException e) {

        }
        return dest;
    }

    /**
     * 判断两个集合的元素个数及元素是否相等(元素顺序可以不一致)
     * 
     * @param collect1 集合1
     * @param collect2 集合2
     * @return
     */
    public static <T> boolean elementEquals(Collection<T> collect1, Collection<T> collect2) {
        if (collect1 == null) {
            return collect2 == null;
        } else {
            if (collect2 == null) {
                return false;
            }
            // 元素个数不相等
            if (collect2.size() != collect1.size()) {
                return false;
            }
            return collect1.containsAll(collect2);
        }
    }

    /**
     * 从给定的集合中,根据映射操作返回一个新的集合. 如提取元素的一个属性组成新的集合.示例:
     * 
     * <pre>
     * {@code
     * CollectionHelper.transform(users, u -> u.getName());
     * }
     * </pre>
     * 
     * @param collection
     * @param mapper
     * @return
     */
    public static <T,R> List<R> transform(Collection<T> collection, Function<? super T,? extends R> mapper) {
        if (CollectionHelper.isEmpty(collection)) {
            throw new IllegalArgumentException("parameter 'collection' not be null");
        }
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
}
