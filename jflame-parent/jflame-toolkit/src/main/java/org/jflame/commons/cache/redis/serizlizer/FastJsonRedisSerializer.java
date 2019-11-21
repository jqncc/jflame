package org.jflame.commons.cache.redis.serizlizer;

import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.jflame.commons.exception.SerializeException;

public class FastJsonRedisSerializer implements IGenericRedisSerializer {

    private final static ParserConfig defaultRedisConfig = new ParserConfig();
    static {
        defaultRedisConfig.setAutoTypeSupport(true);
    }

    public byte[] serialize(Object object) throws SerializeException {
        if (object == null) {
            return new byte[0];
        }
        if (object instanceof byte[]) {
            return (byte[]) object;
        }
        try {
            return JSON.toJSONBytes(object, SerializerFeature.WriteClassName);
        } catch (Exception ex) {
            throw new SerializeException("Could not serialize: " + ex.getMessage(), ex);
        }
    }

    public Object deserialize(byte[] bytes) throws SerializeException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            String text = new String(bytes, StandardCharsets.UTF_8);
            // System.out.println(text);

            return JSON.parseObject(text, Object.class, defaultRedisConfig, Feature.AllowUnQuotedFieldNames);
        } catch (Exception ex) {
            throw new SerializeException("Could not deserialize: " + ex.getMessage(), ex);
        }
    }

    /*public void setAccetTypeName(String acceptTypeName) {
        String[] names = acceptTypeName.split(",");
        for (String name : names) {
            defaultRedisConfig.addAccept(name);
        }
    }*/
}
