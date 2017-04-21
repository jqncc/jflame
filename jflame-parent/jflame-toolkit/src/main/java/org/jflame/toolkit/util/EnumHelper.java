package org.jflame.toolkit.util;

/**
 * 枚举工具类.
 * @see org.apache.commons.lang3.EnumUtils
 * 
 * @author yucan.zhang
 *
 */
public final class EnumHelper {

    /**
     * 获取枚举类型的所有选项名称
     * 
     * @param enumClazz 枚举类型
     * @return
     */
    public static <E extends Enum<E>> String[] enumNames(Class<E> enumClazz) {
        E[] enums = enumClazz.getEnumConstants();
        String[] enumNames = new String[enums.length];
        for (int i = 0; i < enums.length; i++) {
            enumNames[i] = enums[i].name();
        }
        return enumNames;
    }
}
