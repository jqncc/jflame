package org.jflame.toolkit.common.bean;

/**
 * 通用键值对封装bean接口. 常用于字典类型、扩展枚举等
 * 
 * @author yucan.zhang
 * @param <T> 值类型
 */
public interface IKeyValuePair<T> {

    public T getValue();

    public String getName();
}
