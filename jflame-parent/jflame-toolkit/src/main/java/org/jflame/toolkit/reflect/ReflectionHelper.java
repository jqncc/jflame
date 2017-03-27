package org.jflame.toolkit.reflect;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * 反射工具类
 * 
 * @author zyc
 */
public final class ReflectionHelper {

    /**
     * 反射获得Class定义中声明的父类的首个泛型参数的类型. 如无法找到,返回Object.class
     * 
     * @param
     * @return
     */
    @SuppressWarnings({ "unchecked","rawtypes" })
    public static <T> Class<T> getSuperClassGenricType(final Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    /**
     * 反射获得Class定义中声明的父类的指定索引泛型参数的类型. <br>
     * 如无法找到, 返回Object.class. 如public UserDao extends HibernateDao&lt;User,Long&gt;
     * 
     * @param clazz 待查找类型
     * @param index 多个泛型参数时的索引,0开始
     * @return 索引处的泛型参数class对象
     */
    @SuppressWarnings("rawtypes")
    public static Class getSuperClassGenricType(final Class clazz, final int index) {
        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }

        return (Class) params[index];
    }

    /**
     * 直接读取对象属性值
     * 
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static Object getFieldValue(final Object obj, final String fieldName) throws IllegalAccessException {
        /*
         * Field field = getAccessibleField(obj, fieldName); if (field == null) { throw new IllegalArgumentException(
         * "Could not find field [" + fieldName + "] on target [" + obj + "]"); } return field.get(obj);
         */
        return FieldUtils.readField(obj, fieldName, true);
    }

    /**
     * 直接设置对象属性值
     * 
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static void setFieldValue(final Object obj, final String fieldName, final Object value)
            throws IllegalAccessException {
        /*
         * Field field = getAccessibleField(obj, fieldName); if (field == null) { throw new IllegalArgumentException(
         * "Could not find field [" + fieldName + "] on target [" + obj + "]"); } field.set(obj, value);
         */
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
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                // Field不在当前类定义,继续向上转型
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取类型的所有属性描述PropertyDescriptor.
     * 
     * @param clazz 类型
     * @param <T> class泛型
     * @return PropertyDescriptor[]
     * @throws IntrospectionException bean内省过程异常
     */
    public static <T> PropertyDescriptor[] getBeanPropertyDescriptor(Class<T> clazz) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        return beanInfo.getPropertyDescriptors();
    }

}
