package org.jflame.toolkit.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
                    descriptor.getWriteMethod().invoke(newObj, args);
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
     * @return
     */
    public static Map<String,Object> convertBeanToMap(Object beanObj) {
        if (beanObj == null) {
            throw new IllegalArgumentException("不能转换为null的对象");
        }
        Map<String,Object> resultMap = new HashMap<>();
        final String clz = "class";
        Method readMethod;
        Object value;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(beanObj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : propertyDescriptors) {
                if (!clz.equals(pd.getName())) {
                    readMethod = pd.getReadMethod();
                    value = readMethod.invoke(beanObj, new Object[0]);
                    resultMap.put(pd.getName(), value);
                }
            }
        } catch (Exception e) {
            throw new ConvertException(e);
        }
        return resultMap;
    }

}
