package org.jflame.toolkit.cache.serialize;

import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.serializer.RedisSerializer;

import org.jflame.toolkit.exception.SerializeException;

public class SpringRedisSerializer implements IGenericRedisSerializer {

    RedisSerializer<Object> genericSerializer;

    public SpringRedisSerializer(RedisSerializer<Object> genericSerializer) {
        this.genericSerializer = genericSerializer;
    }

    @Override
    public byte[] serialize(Object t) throws SerializeException {
        return genericSerializer.serialize(t);
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializeException {
        System.out.println("====" + new String(bytes, StandardCharsets.UTF_8));
        return genericSerializer.deserialize(bytes);
    }

}
