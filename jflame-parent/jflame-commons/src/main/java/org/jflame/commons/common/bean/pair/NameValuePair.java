package org.jflame.commons.common.bean.pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 字符串键值对封装bean
 * 
 * @author yucan.zhang
 */
public class NameValuePair extends StrKeyPair<String> implements Serializable {

    private static final long serialVersionUID = -6678830111869172034L;

    public NameValuePair(String name, String value) {
        super(name, value);
    }

    public String name() {
        return getKey();
    }

    public String value() {
        return getValue();
    }

    /**
     * Map&lt;String,?&gt;转为List&lt;NameValuePair&gt;忽略null键.
     * 
     * @param map Map&lt;String,?&gt;
     * @return NameValuePair列表
     */
    public static List<NameValuePair> toList(Map<String,String> map) {
        List<NameValuePair> nvpirs = new ArrayList<>();
        if (map != null) {
            for (Entry<String,String> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    nvpirs.add(new NameValuePair(entry.getKey(), entry.getValue()));
                }
            }
        }
        return nvpirs;
    }

}
