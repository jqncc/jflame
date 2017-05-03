package org.jflame.toolkit.common.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jflame.toolkit.util.CollectionHelper;

/**
 * 通用键值对封装bean接口. 常用于字典类型、扩展枚举等
 * 
 * @author yucan.zhang
 * @param <T> 值类型
 */
public class KeyValuePair<K,V> {

    private K name;
    private V value;

    public KeyValuePair(K name, V value) {
        this.name = name;
        this.value = value;
    }

    public K getName() {
        return name;
    }

    public V getValue() {
        return value;
    }

    public void setName(K name) {
        this.name = name;
    }

    public void setValue(V value) {
        this.value = value;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @SuppressWarnings("rawtypes")
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
        KeyValuePair other = (KeyValuePair) obj;
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


    /**
     * List&lt;IKeyValuePair&gt;转为Map&lt;String,String&gt;,以name为key,注意相同name将会丢失值.
     * @param <R>
     * 
     * @param list IKeyValuePair列表
     * @return Map
     */
    public static <E extends KeyValuePair<T,R>,T, R> Map<T,R> toMap(List<E> list) {
        Map<T, R> map = new HashMap<>();
        if (list != null) {
            for (KeyValuePair<T, R> nvp : list) {
                map.put(nvp.getName(), nvp.getValue());
            }
        }
        return map;
    }

    /**
     * 转为url参数格式的字符串
     * 
     * @param list
     * @return
     */
    public static <E extends KeyValuePair<T,R>,T, R> String toUrlParam(List<E> list) {
        if (CollectionHelper.isNotNullAndEmpty(list)) {
            StringBuilder strBuf = new StringBuilder(20);
            for (KeyValuePair<T, R> kv : list) {
                strBuf.append('&').append(kv.getName()).append('=').append(kv.getValue());
            }
            strBuf.deleteCharAt(0);
            return strBuf.toString();
        }
        return "";
    }
}
