package org.jflame.toolkit.util;

import org.apache.commons.lang3.EnumUtils;

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
}
