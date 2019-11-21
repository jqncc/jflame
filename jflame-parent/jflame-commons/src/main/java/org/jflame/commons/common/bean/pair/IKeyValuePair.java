package org.jflame.commons.common.bean.pair;

/**
 * 通用键值对类型接口
 * @author yucan.zhang
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface IKeyValuePair<K,V> {

    K getKey();

    V getValue();

}