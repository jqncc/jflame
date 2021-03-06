package org.jflame.commons.util;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.jflame.commons.crypto.DigestHelper;
import org.jflame.commons.model.Chars;

public class SignHelper {

    public static String mapParamSign(Map<String,?> mapData) {
        return mapParamSign(mapData, null);
    }

    /**
     * <p>
     * map参数签名,签名算法: 以key按单词自然排序后,将key与value用=和&amp;接接, 再对字符串做md5,null值忽略.<br>
     * 如:{@code md5hex(k1=v1&k2=v2&)}
     * </p>
     * <p>
     * 注:时间格式(Date,LocalDateTime)默认格式:yyyyMMddHHmmss,日期格式(LocalDate,java.sql.Date):yyyyMMdd
     * 
     * @param mapData 待签名的map
     * @param excludeKeys 不参与签名的map key
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String mapParamSign(Map<String,?> mapData, String[] excludeKeys) {
        if (MapHelper.isEmpty(mapData)) {
            return null;
        }
        StringBuilder str = new StringBuilder(30);
        SortedMap<String,Object> sortedMap;
        if (mapData instanceof TreeMap) {
            sortedMap = (TreeMap<String,Object>) mapData;
        } else {
            sortedMap = new TreeMap<>(mapData);
        }

        boolean hasExclude = ArrayUtils.isNotEmpty(excludeKeys);
        boolean isArray = false;
        for (Entry<String,?> kv : sortedMap.entrySet()) {
            if (hasExclude && ArrayUtils.contains(excludeKeys, kv.getKey())) {
                continue;
            }
            // 值为null忽略
            if (kv.getValue() == null || StringUtils.EMPTY.equals(kv.getValue())) {
                continue;
            }
            // 数组长度为0忽略
            isArray = kv.getValue()
                    .getClass()
                    .isArray();
            if (isArray && Array.getLength(kv.getValue()) == 0) {
                continue;
            }
            str.append(Chars.AND);
            str.append(kv.getKey())
                    .append(Chars.EQUAL);
            if (kv.getValue() instanceof BigDecimal) {
                str.append(((BigDecimal) kv.getValue()).stripTrailingZeros()
                        .toPlainString());
            }
            if (kv.getValue() instanceof Date) {
                str.append(DateHelper.format((Date) kv.getValue(), DateHelper.yyyyMMddHHmmss));
            } else if (kv.getValue() instanceof LocalDateTime) {
                str.append(DateHelper.format((LocalDateTime) kv.getValue(), DateHelper.yyyyMMddHHmmss));
            } else if (kv.getValue() instanceof LocalDate) {
                str.append(DateHelper.format((LocalDate) kv.getValue(), DateHelper.yyyyMMdd));
            } else if (kv.getValue() instanceof java.sql.Date) {
                str.append(DateHelper.format((java.sql.Date) kv.getValue(), DateHelper.yyyyMMdd));
            } else if (isArray) {
                str.append(toArrayString(kv.getValue()));
            } else {
                str.append(kv.getValue());
            }
        }

        return DigestHelper.md5Hex(str.substring(1));
    }

    /**
     * 未知元素类型的数组toString.字符串组成同Arrays.toString()
     *
     * @param a 数组
     * @return
     */
    private static String toArrayString(Object a) {
        if (a == null)
            return "null";
        int iMax = Array.getLength(a) - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0;; i++) {
            b.append(Array.get(a, i));
            if (i == iMax)
                return b.append(']')
                        .toString();
            b.append(", ");
        }
    }

}
