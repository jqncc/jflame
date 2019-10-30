package org.jflame.toolkit.cache.serialize;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import org.jflame.toolkit.exception.SerializeException;

public class SpringRedisJsonSerializer extends FastJsonRedisSerializer implements RedisSerializer<Object> {

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        try {
            return super.serialize(object);
        } catch (SerializeException ex) {
            throw new SerializationException(ex.getMessage());
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        try {
            return super.deserialize(bytes);
        } catch (SerializeException ex) {
            throw new SerializationException(ex.getMessage());
        }
    }
}
