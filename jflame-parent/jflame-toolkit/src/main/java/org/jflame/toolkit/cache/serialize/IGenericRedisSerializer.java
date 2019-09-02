package org.jflame.toolkit.cache.serialize;

import org.jflame.toolkit.exception.SerializeException;

/**
 * 通用对象序列化接口
 * 
 * @author yucan.zhang
 */
public interface IGenericRedisSerializer extends IRedisSerializer<Object> {

    byte[] serialize(Object t) throws SerializeException;

    Object deserialize(byte[] bytes) throws SerializeException;
}
