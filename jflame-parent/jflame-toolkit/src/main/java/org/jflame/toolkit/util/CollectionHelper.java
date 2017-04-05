package org.jflame.toolkit.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

public final class CollectionHelper {

    /**
     * 判断集合是否为null或无元素
     * 
     * @param collection 集合
     * @param <E> 泛型
     * @return
     */
    public static <E> boolean isNullOrEmpty(Collection<E> collection) {
        return collection == null || collection.size() == 0;
    }

    /**
     * 判断集合不为null且至少有一个元素
     * 
     * @param collection 集合
     * @param <E> 泛型
     * @return
     */
    public static <E> boolean isNotNullAndEmpty(Collection<E> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * 枚举集合转数组,数组元素类型必须与集合元素相同或是其实现类
     * 
     * @param enumeration 枚举集合
     * @param array 目标数组    
     * @return
     */
    public static <A,E extends A> A[] toArray(Enumeration<E> enumeration, A[] array) {
        ArrayList<A> elements = new ArrayList<A>();
        while (enumeration.hasMoreElements()) {
            elements.add(enumeration.nextElement());
        }
        return elements.toArray(array);
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

        final Class<?> type1 = arr1.getClass().getComponentType();
        T[] joinedArray = (T[]) Array.newInstance(type1, len);

        for (T t : newSet) {
            joinedArray[i++] = t;
        }
        return joinedArray;
    }
}
