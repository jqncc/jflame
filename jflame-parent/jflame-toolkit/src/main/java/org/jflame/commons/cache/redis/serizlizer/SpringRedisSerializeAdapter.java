package org.jflame.commons.cache.redis.serizlizer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import org.jflame.commons.exception.SerializeException;

public class SpringRedisSerializeAdapter<T> implements RedisSerializer<T> {

    IRedisSerializer<T> mySerializer;

    public SpringRedisSerializeAdapter(IRedisSerializer<T> mySerializer) {
        this.mySerializer = mySerializer;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        try {
            return mySerializer.serialize(t);
        } catch (SerializeException ex) {
            throw new SerializationException(ex.getMessage());
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        try {
            return mySerializer.deserialize(bytes);
        } catch (SerializeException ex) {
            throw new SerializationException(ex.getMessage());
        }
    }

}
