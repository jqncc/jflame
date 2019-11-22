package org.jflame.commons.util;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

import org.jflame.commons.exception.SerializeException;

public class Jacksons {

    private ObjectMapper objMapper = new ObjectMapper();

    public Jacksons() {
        objMapper.configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        // 忽略空Bean转json的错误
        objMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    public String toJson(Object obj) {
        try {
            return objMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new SerializeException(e);
        }
    }

    public String toJson(Object obj, boolean ignoreNull, SerializationFeature... features) {
        objMapper.setSerializationInclusion(Include.NON_NULL);
        if (features != null) {
            for (SerializationFeature feature : features) {
                objMapper.enable(feature);
            }
        }
        try {
            return objMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new SerializeException(e);
        }
    }

    public byte[] toJsonBytes(Object obj) {
        try {
            return objMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new SerializeException(e);
        }
    }

    public final void writeJSONString(OutputStream os, Object object) throws IOException {
        objMapper.writeValue(os, object);
    }

    public <T> T parseObject(String jsonStr, Class<T> clazz) {
        try {
            return objMapper.readValue(jsonStr, clazz);
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    public <T> List<T> parseList(String jsonStr, Class<T> elementClazz) {
        CollectionType collType = objMapper.getTypeFactory()
                .constructCollectionType(ArrayList.class, elementClazz);
        try {
            return objMapper.readValue(jsonStr, collType);
        } catch (IOException e) {
            throw new SerializeException(e);
        }

    }

    public <K,V> Map<K,V> parseMap(String jsonStr, Class<K> keyClazz, Class<V> valueClazz) {
        MapType mapType = objMapper.getTypeFactory()
                .constructMapType(HashMap.class, keyClazz, valueClazz);
        try {
            return objMapper.readValue(jsonStr, mapType);
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    public <T> T parse(String jsonStr, TypeReference<T> typeRef) {
        try {
            return objMapper.readValue(jsonStr, typeRef);
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    public ObjectMapper getObjectMapper() {
        return objMapper;
    }

    /**
     * 启用
     * 
     * @param first
     * @param other
     * @return
     */
    public Jacksons enable(SerializationFeature first, SerializationFeature... other) {
        objMapper.enable(first, other);
        return this;
    }

    public Jacksons enable(Feature... feature) {
        objMapper.enable(feature);
        return this;
    }

    public Jacksons disable(SerializationFeature first, SerializationFeature... other) {
        objMapper.disable(first, other);
        return this;
    }

    public Jacksons disable(Feature... feature) {
        objMapper.disable(feature);
        return this;
    }

    /**
     * 启用日期按格式输出,默认格式yyyy-MM-dd HH:mm:ss
     * 
     * @return
     */
    public Jacksons dateFormat() {
        objMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objMapper.setDateFormat(new SimpleDateFormat(DateHelper.YYYY_MM_DD_HH_mm_ss));
        return this;
    }

    /**
     * 启用日期按格式输出,并指定日期格式
     * 
     * @param format
     */
    public Jacksons dateFormat(String datePattern) {
        if (StringHelper.isEmpty(datePattern)) {
            return dateFormat();
        }
        objMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objMapper.setDateFormat(new SimpleDateFormat(datePattern));
        return this;
    }

    /**
     * 忽略值为null的属性
     * 
     * @return
     */
    public Jacksons ignoreNull() {
        objMapper.setSerializationInclusion(Include.NON_NULL);
        return this;
    }

}
