package org.jflame.toolkit.util;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.jflame.toolkit.crypto.DigestHelper;

public class SignHelper {

    public static String mapParamSign(Map<String,?> mapData) {
        return mapParamSign(mapData, null);
    }

    /**
     * map参数签名
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
        final char[] splitChars = { '=','&' };
        boolean hasExclude = ArrayUtils.isNotEmpty(excludeKeys);
        for (Entry<String,?> kv : sortedMap.entrySet()) {
            if (hasExclude && ArrayUtils.contains(excludeKeys, kv.getKey())) {
                continue;
            }
            if (kv.getValue() != null) {
                str.append(kv.getKey())
                        .append(splitChars[0]);
                if (kv.getValue() instanceof BigDecimal) {
                    str.append(((BigDecimal) kv.getValue()).stripTrailingZeros()
                            .toPlainString());
                } else {
                    str.append(kv.getValue());
                }
                str.append(splitChars[1]);
            }
        }
        System.out.println("sign:" + str.toString());
        return DigestHelper.md5Hex(str.toString());
    }
}
