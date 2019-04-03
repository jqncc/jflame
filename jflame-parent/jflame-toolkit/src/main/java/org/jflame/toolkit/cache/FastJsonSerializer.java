package org.jflame.toolkit.cache;

import java.nio.charset.StandardCharsets;

import org.jflame.toolkit.exception.SerializeException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class FastJsonSerializer implements IGenericSerializer {

    private final static ParserConfig defaultRedisConfig = new ParserConfig();
    static {
        defaultRedisConfig.setAutoTypeSupport(true);
    }

    public byte[] serialize(Object object) throws SerializeException {
        if (object == null) {
            return new byte[0];
        }
        try {
            return JSON.toJSONBytes(object, SerializerFeature.WriteClassName, SerializerFeature.WriteMapNullValue);
        } catch (Exception ex) {
            throw new SerializeException("Could not serialize: " + ex.getMessage(), ex);
        }
    }

    public Object deserialize(byte[] bytes) throws SerializeException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), Object.class, defaultRedisConfig);
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
