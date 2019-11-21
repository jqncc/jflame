package org.jflame.commons.cache.redis.serizlizer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jflame.commons.exception.SerializeException;

public class StringRedisSerializer implements IRedisSerializer<String> {

    private final Charset charset;

    public StringRedisSerializer() {
        this(StandardCharsets.UTF_8);
    }

    public StringRedisSerializer(Charset charset) {
        if (charset == null) {
            this.charset = StandardCharsets.UTF_8;
        } else {
            this.charset = charset;
        }
    }

    @Override
    public byte[] serialize(String string) throws SerializeException {
        return (string == null ? null : string.getBytes(charset));
    }

    @Override
    public String deserialize(byte[] bytes) throws SerializeException {
        return (bytes == null ? null : new String(bytes, charset));
    }

}
