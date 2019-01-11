package org.jflame.toolkit.common.bean.pair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jflame.toolkit.util.CollectionHelper;

/**
 * 通用键值对封装bean接口. 常用于字典类型、扩展枚举等
 * 
 * @author yucan.zhang
 */
public class KeyValuePair<K,V> implements IKeyValuePair<K,V> {

    private K key;
    private V value;

    public KeyValuePair(K name, V value) {
        this.key = name;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    public void setKey(K name) {
        this.key = name;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
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
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
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
        return "[key=" + key + ", value=" + value + "]";
    }

    /**
     * 根据属性key的值获取对应枚举
     * 
     * @param enumClazz 实现IKeyValuePair接口的枚举类型
     * @param key 属性key值
     * @throws IllegalArgumentException 传入类型未实现IKeyValuePair接口
     * @return 返回实现IKeyValuePair接口的枚举,如果没有对应值将返回null
     */
    public static <E extends Enum<E> & IKeyValuePair<K,V>,K,V> E getEnumByKey(Class<E> enumClazz, K key) {
        if (IKeyValuePair.class.isAssignableFrom(enumClazz)) {
            E[] enums = enumClazz.getEnumConstants();
            for (E e : enums) {
                IKeyValuePair<K,?> pair = (IKeyValuePair<K,?>) e;
                if (pair.getKey()
                        .equals(key)) {
                    return e;
                }
            }
        } else {
            throw new IllegalArgumentException("参数enumClazz必须实现接口IKeyValuePair");
        }
        return null;
    }

    /**
     * List&lt;KeyValuePair&lt;T,R&gt;&gt;转为Map&lt;T,R&gt;注意相同key将会丢失值.
     * 
     * @param <R>
     * @param list KeyValuePair列表
     * @return Map
     */
    public static <E extends KeyValuePair<T,R>,T,R> Map<T,R> toMap(List<E> list) {
        Map<T,R> map = new HashMap<>();
        if (list != null) {
            for (IKeyValuePair<T,R> nvp : list) {
                map.put(nvp.getKey(), nvp.getValue());
            }
        }
        return map;
    }

    /**
     * Map&lt;T,R&gt;List&lt;转为KeyValuePair&lt;T,R&gt;&gt;
     * 
     * @param map
     * @return
     */
    public static <K,V> List<KeyValuePair<K,V>> fromMap(Map<K,V> map) {
        List<KeyValuePair<K,V>> results = new ArrayList<>();
        KeyValuePair<K,V> pair;
        for (Entry<K,V> item : map.entrySet()) {
            pair = new KeyValuePair<K,V>(item.getKey(), item.getValue());
            results.add(pair);
        }
        return results;
    }

    /**
     * 转为url参数格式的字符串
     * 
     * @param list
     * @return
     */
    @Deprecated
    public static <E extends KeyValuePair<T,R>,T,R> String toUrlParam(List<E> list) {
        if (CollectionHelper.isNotEmpty(list)) {
            StringBuilder strBuf = new StringBuilder(20);
            try {
                for (IKeyValuePair<T,R> kv : list) {
                    strBuf.append('&')
                            .append(kv.getKey())
                            .append('=')
                            .append(URLEncoder.encode(kv.getValue()
                                    .toString(), StandardCharsets.UTF_8.name()));
                }
            } catch (UnsupportedEncodingException e) {
                // ignore
            }
            strBuf.deleteCharAt(0);
            return strBuf.toString();
        }
        return "";
    }

}
