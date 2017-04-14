package org.jflame.toolkit.common.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jflame.toolkit.util.CollectionHelper;

/**
 * 字符串键值对封装bean
 * 
 * @author yucan.zhang
 */
public class NameValuePair implements IKeyValuePair<String>, Serializable {

    private static final long serialVersionUID = -6678830111869172034L;

    private String name;
    private String value;

    public NameValuePair() {
    }

    public NameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    /**
     * List&lt;NameValuePair&gt;转为Map&lt;String,String&gt;,以name为key,注意相同name将会丢失值.
     * 
     * @param list NameValuePair列表
     * @return Map
     */
    public static Map<String,String> toMap(List<NameValuePair> list) {
        Map<String,String> map = new HashMap<String,String>();
        if (list != null) {
            for (NameValuePair nvp : list) {
                map.put(nvp.getName(), nvp.getValue());
            }
        }
        return map;
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

    /**
     * 转为url参数格式的字符串
     * 
     * @param list
     * @return
     */
    public static String toUrlParam(List<NameValuePair> list) {
        if (CollectionHelper.isNotNullAndEmpty(list)) {
            StringBuilder strBuf = new StringBuilder(20);
            for (NameValuePair kv : list) {
                strBuf.append('&').append(kv.getName()).append('=').append(kv.getValue());
            }
            strBuf.deleteCharAt(0);
            return strBuf.toString();
        }
        return "";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NameValuePair other = (NameValuePair) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[name=" + name + ", value=" + value + "]";
    }

}
