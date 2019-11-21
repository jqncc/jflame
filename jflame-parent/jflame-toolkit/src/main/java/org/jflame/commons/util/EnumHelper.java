package org.jflame.commons.util;

import java.util.HashMap;
import java.util.Map;

import org.jflame.commons.common.bean.pair.IKeyValuePair;

/**
 * 枚举工具类.
 * 
 * @see org.apache.commons.lang3.EnumUtils
 * @author yucan.zhang
 */
public final class EnumHelper {

    /**
     * 获取枚举类型的所有选项名称,如果参数enumClazz不是枚举对象将返回null
     * 
     * @param enumClazz 枚举类型
     * @return
     */
    public static <E extends Enum<E>> String[] enumNames(Class<E> enumClazz) {
        E[] enums = enumClazz.getEnumConstants();
        if (enums != null) {
            String[] enumNames = new String[enums.length];
            for (int i = 0; i < enums.length; i++) {
                enumNames[i] = enums[i].name();
            }
            return enumNames;
        }
        return null;
    }

    /**
     * 根据枚举位置索引值ordinal获取枚举对象,如果参数enumClazz不是枚举对象将返回null
     * 
     * @param enumClazz 枚举类型
     * @param ordinal 枚举ordinal值
     * @return
     */
    public static <E extends Enum<E>> E getEnumByOrdinal(Class<E> enumClazz, int ordinal) {
        E[] enums = enumClazz.getEnumConstants();
        if (enums != null) {
            for (E e : enums) {
                if (e.ordinal() == ordinal) {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * 将实现了IKeyValuePair接口的枚举所有值转为以枚举name为key,getValue()为值的map
     * 
     * @param enumClazz 实现了IKeyValuePair接口的枚举
     * @return
     */
    public static <E extends Enum<E> & IKeyValuePair<K,V>,K,V> Map<String,V> pairToMap(Class<E> enumClazz) {
        if (IKeyValuePair.class.isAssignableFrom(enumClazz)) {
            E[] enums = enumClazz.getEnumConstants();
            Map<String,V> map = new HashMap<>(enums.length);
            for (E e : enums) {
                IKeyValuePair<K,V> pair = (IKeyValuePair<K,V>) e;
                map.put(e.name(), pair.getValue());
            }
            return map;
        } else {
            throw new IllegalArgumentException("参数enumClazz必须实现接口IKeyValuePair");
        }
    }
}
