package org.jflame.commons.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * 反射工具类
 * 
 * @see org.apache.commons.lang3.reflect.FieldUtils
 * @author zyc
 */
public final class ReflectionHelper {

    /**
     * 获取所实现接口的首个泛型参数的类型，如无法找到返回null
     * 
     * @param clazz 待查找类型
     * @return
     */
    @SuppressWarnings({ "unchecked","rawtypes" })
    public static <T> Class<T> getIntefaceGenricType(final Class clazz) {
        try {
            Type[] types = clazz.getGenericInterfaces();
            for (Type type : types) {
                if (type instanceof ParameterizedType) {
                    Type[] params = ((ParameterizedType) type).getActualTypeArguments();
                    return ArrayUtils.isEmpty(params) ? null : (Class) params[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取父类的首个泛型参数的类型，如无法找到返回null
     * 
     * @param clazz 待查找类型
     * @return
     */
    @SuppressWarnings({ "unchecked","rawtypes" })
    public static <T> Class<T> getSuperClassGenricType(final Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    /**
     * 反射获得Class定义中声明的父类的指定索引泛型参数的类型.无泛型或索引超出返回null <br>
     * 
     * @param clazz 待查找类型
     * @param index 多个泛型参数时的索引,0开始
     * @return 索引处的泛型参数class对象
     */
    @SuppressWarnings("rawtypes")
    public static Class getSuperClassGenricType(final Class clazz, int index) {
        if (index < 0) {
            index = 0;
        }
        try {
            Type genType = clazz.getGenericSuperclass();
            if (genType instanceof ParameterizedType) {
                Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
                if (params.length <= index) {
                    return (Class) params[index];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 直接读取对象属性值
     * 
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static Object getFieldValue(final Object obj, final String fieldName) throws IllegalAccessException {
        return FieldUtils.readField(obj, fieldName, true);
    }

    /**
     * 直接设置对象属性值
     * 
     * @throws IllegalAccessException
     * @throws IllegalArgumentException 目标或成员名为null
     */
    public static void setFieldValue(final Object obj, final String fieldName, final Object value)
            throws IllegalAccessException {
        FieldUtils.writeField(obj, fieldName, value, true);
    }

    /**
     * 获取对象的DeclaredField,并强制设置为可访问,如向上转型到Object仍无法找到, 返回null.
     * 
     * @param obj 对象
     * @param fieldName 属性名
     * @return Field
     */
    public static Field getAccessibleField(final Object obj, final String fieldName) {
        return FieldUtils.getField(obj.getClass(), fieldName, true);
    }

}
