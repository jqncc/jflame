package org.jflame.toolkit.reflect;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.ClassUtils;
import org.jflame.toolkit.exception.BeanAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * javabean操作 工具类
 * 
 * @author yucan.zhang
 */
public class BeanHelper {

    private final static Logger log = LoggerFactory.getLogger(BeanHelper.class);

    /**
     * 对象属性复制
     * 
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreProperties 要忽略的属性
     * @throws BeanAccessException 复制失败
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties)
            throws BeanAccessException {
        PropertyDescriptor[] targetPds = getPropertyDescriptors(target.getClass());
        PropertyDescriptor[] sourcetPds = getPropertyDescriptors(source.getClass());
        List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : null);
        if (targetPds != null && sourcetPds != null) {
            for (PropertyDescriptor targetPd : targetPds) {
                Method writeMethod = targetPd.getWriteMethod();
                if (writeMethod != null && (ignoreList == null || !ignoreList.contains(targetPd.getName()))) {
                    PropertyDescriptor sourcePd = getPropertyByName(sourcetPds, targetPd.getName());
                    if (sourcePd != null) {
                        Method readMethod = sourcePd.getReadMethod();
                        if (readMethod != null && ClassUtils.isAssignable(writeMethod.getParameterTypes()[0],
                                readMethod.getReturnType())) {
                            try {
                                if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                    readMethod.setAccessible(true);
                                }
                                Object value = readMethod.invoke(source);
                                if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                    writeMethod.setAccessible(true);
                                }
                                writeMethod.invoke(target, value);
                            } catch (Throwable ex) {
                                throw new BeanAccessException("不能复制属性:" + targetPd.getName(), ex);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取类型的所有属性描述PropertyDescriptor,内省异常返回null.
     * 
     * @param clazz 类型
     * @param <T> class泛型
     * @return PropertyDescriptor[],内省异常返回null.
     */
    public static <T> PropertyDescriptor[] getPropertyDescriptors(Class<T> clazz) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
            return beanInfo.getPropertyDescriptors();
        } catch (IntrospectionException e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * 获取某属性的描述PropertyDescriptor,属性不存在返回null.
     * 
     * @param clazz 类型
     * @param propertyName 属性名
     * @return
     */
    public static <T> PropertyDescriptor getPropertyDescriptor(Class<T> clazz, String propertyName) {
        PropertyDescriptor[] pds = getPropertyDescriptors(clazz);
        if (pds != null) {
            for (PropertyDescriptor pd : pds) {
                if (pd.getName().equals(propertyName)) {
                    return pd;
                }
            }
        }
        return null;
    }

    /**
     * 判断是否是简单值类型.包括：基础数据类型、CharSequence、Number、Date、URL、URI、Locale、Class;
     * 
     * @param clazz
     * @return
     */
    public static boolean isSimpleValueType(Class<?> clazz) {
        return (ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isEnum() || CharSequence.class.isAssignableFrom(clazz)
                || Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz) || URI.class == clazz
                || URL.class == clazz || Locale.class == clazz || Class.class == clazz);
    }

    private static PropertyDescriptor getPropertyByName(PropertyDescriptor[] pds, String propertyName) {
        for (PropertyDescriptor pd : pds) {
            if (pd.getName().equals(propertyName)) {
                return pd;
            }
        }
        return null;
    }
}
