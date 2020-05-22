package org.jflame.apidoc.enums;

import org.jflame.apidoc.util.ArrayUtils;

import com.sun.javadoc.Type;

/**
 * 可表示的数据类型枚举
 * 
 * @author yucan.zhang
 *
 */
public enum JsonDataType {
    string, number, object, array, bool, date;

    // byte ， double ， float ， int ， long和short
    public static String num_java_types[] = { "java.lang.Byte","java.lang.Number","java.lang.Long","java.lang.Integer",
            "java.math.BigInteger","java.lang.Short","java.lang.Float","java.lang.Double","java.math.BigDecimal" };
    public static String list_java_types[] = { "java.util.List","java.util.ArrayList","java.util.Set" };

    public static JsonDataType toType(Type type) {
        String javaTypeName = type.qualifiedTypeName();
        if (javaTypeName.startsWith("java.")) {
            if ("java.lang.String".equals(javaTypeName)) {
                return JsonDataType.string;
            } else if ("java.lang.Boolean".equals(javaTypeName)) {
                return JsonDataType.bool;
            } else if (ArrayUtils.contains(num_java_types, javaTypeName)) {
                return JsonDataType.number;
            } else if (ArrayUtils.contains(list_java_types, javaTypeName)) {
                return JsonDataType.array;
            }
        }

        return JsonDataType.object;
    }
}
