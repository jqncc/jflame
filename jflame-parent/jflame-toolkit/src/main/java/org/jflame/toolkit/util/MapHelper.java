package org.jflame.toolkit.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.jflame.toolkit.exception.ConvertException;

/**
 * Map工具类
 * 
 * @author yucan.zhang
 */
public final class MapHelper {

    /**
     * 判断Map是否为null或无元素
     * 
     * @param map Map
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断Map是否不为null且至少有一个元素
     * 
     * @param map Map
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean isNotEmpty(Map map) {
        return map != null && !map.isEmpty();
    }

    /**
     * 将map转为javabean对象
     * 
     * @param map Map
     * @param <T> 目标Bean泛型
     * @param beanClass bean类型
     * @return
     */
    public static <T> T convertMapToBean(Map<String,Object> map, Class<T> beanClass) {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("待转换的map为null");
        }
        Object value;
        Object[] args = new Object[1];
        String propertyName;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
            T newObj = beanClass.newInstance();
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                PropertyDescriptor descriptor = propertyDescriptors[i];
                propertyName = descriptor.getName();
                if (map.containsKey(propertyName)) {
                    value = map.get(propertyName);
                    args[0] = value;
                    if (descriptor.getWriteMethod() != null) {
                        descriptor.getWriteMethod()
                                .invoke(newObj, args);
                    }
                }
            }
            return newObj;

        } catch (Exception e) {
            throw new ConvertException(e);
        }
    }

    /**
     * 将javabean对象转为map
     * 
     * @param beanObj bean对象
     * @param excludes 要排除的属性名
     * @return
     */
    public static Map<String,Object> convertBeanToMap(Serializable beanObj, String... excludes) {
        return convertBeanToMap(beanObj, false, false, excludes);
    }

    /**
     * 将javabean对象转为map
     * 
     * @param beanObj bean对象
     * @param ignoreNullValue 是否忽略值为null的属性,true=忽略
     * @param excludes 要排除的属性名
     * @return
     */
    public static Map<String,Object> convertBeanToMap(Serializable beanObj, boolean ignoreNullValue,
            boolean isSortedMap, String... excludes) {
        if (beanObj == null) {
            throw new IllegalArgumentException("不能转换为null的对象");
        }
        Map<String,Object> resultMap = isSortedMap ? new TreeMap<>() : new HashMap<>();
        Set<String> excludeProps = new HashSet<>();
        excludeProps.add("class");
        if (ArrayUtils.isNotEmpty(excludes)) {
            Collections.addAll(excludeProps, excludes);
        }
        Method readMethod;
        Object value;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(beanObj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : propertyDescriptors) {
                if (!excludeProps.contains(pd.getName())) {
                    readMethod = pd.getReadMethod();
                    if (readMethod != null) {
                        value = readMethod.invoke(beanObj, new Object[0]);
                        if (ignoreNullValue && value == null) {
                            continue;
                        }
                        resultMap.put(pd.getName(), value);
                    }
                }
            }
        } catch (Exception e) {
            throw new ConvertException(e);
        }
        return resultMap;
    }

    /**
     * 将javabean对象转按键自然排序的treemap
     * 
     * @param beanObj
     * @param ignoreNullValue
     * @param excludes
     * @return
     */
    public static TreeMap<String,Object> convertBeanToSortedMap(Serializable beanObj, boolean ignoreNullValue,
            String... excludes) {
        return (TreeMap<String,Object>) convertBeanToMap(beanObj, ignoreNullValue, true, excludes);
    }
}
