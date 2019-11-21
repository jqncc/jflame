package org.jflame.commons.cache.redis.serizlizer;

import org.jflame.commons.exception.SerializeException;

public interface IRedisSerializer<T> {

    /**
     * 序列化一个对象为二进制数组
     * 
     * @param t 要序列化的对象
     * @return byte[]
     */
    byte[] serialize(T t) throws SerializeException;

    /**
     * 反序列化二制数组为java对象
     * 
     * @param bytes byte[]
     * @return 反序列化后对象
     * @throws SerializeException
     */
    T deserialize(byte[] bytes) throws SerializeException;
}
