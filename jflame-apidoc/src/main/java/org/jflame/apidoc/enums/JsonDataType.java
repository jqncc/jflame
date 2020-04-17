package org.jflame.apidoc.enums;

import org.jflame.apidoc.util.ArrayUtils;

import com.sun.javadoc.Type;

/**
 * 可表示的数据类型枚举
 * 
 * @author yucan.zhang
 *
 */
public enum DataType {
    string, number, object, array, bool, date;

    // byte ， double ， float ， int ， long和short
    public static String num_java_types[] = { "java.lang.Byte","java.lang.Number","java.lang.Long","java.lang.Integer",
            "java.lang.Short","java.lang.Float","java.lang.Double","java.math.BigDecimal" };

    public static DataType toType(Type type) {
        String javaTypeName = type.qualifiedTypeName();
        if (javaTypeName.startsWith("java.")) {

            if (ArrayUtils.contains(num_java_types, javaTypeName)) {
                return DataType.number;
            }
        }
        return DataType.object;
    }
}
