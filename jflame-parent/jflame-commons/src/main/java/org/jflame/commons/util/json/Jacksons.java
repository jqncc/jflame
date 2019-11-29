package org.jflame.commons.util.json;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

import org.jflame.commons.common.TypeRef;
import org.jflame.commons.exception.SerializeException;
import org.jflame.commons.util.DateHelper;
import org.jflame.commons.util.IOHelper;
import org.jflame.commons.util.StringHelper;

/**
 * 基于jackson实现json工具类
 * 
 * @author yucan.zhang
 */
public class Jacksons implements Jsons {

    private ObjectMapper objMapper = new ObjectMapper();// 同一个ObjectMapper实例部分配置使用后再修改不生效

    public Jacksons() {
        objMapper.configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        objMapper.configure(Feature.IGNORE_UNKNOWN, true);
        objMapper.setSerializationInclusion(Include.NON_NULL);

        // 忽略空Bean转json的错误
        objMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    @Override
    public String toJson(Object obj) {
        try {
            // return objMapper.setSerializationInclusion(Include.ALWAYS)
            // .writeValueAsString(obj);
            return objMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new SerializeException(e);
        }
    }

    /**
     * Java对象序列化为JSON字符串,按属性组过滤.
     * <p>
     * 配合注解@JsonView使用,如:@JsonView(Views.Default.class)
     * 
     * @param obj 要序列化的Java对象
     * @param viewClass 表示属性组的class,即注解@JsonView中的class
     * @return
     */
    public String toJsonView(Object obj, Class<?> viewClass) {
        try {
            return objMapper.writerWithView(viewClass)
                    .writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public byte[] toJsonBytes(Object obj) {
        try {
            return objMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public void writeJSONString(OutputStream os, Object object) throws IOException {
        try {
            objMapper.writeValue(os, object);
        } finally {
            IOHelper.closeQuietly(os);
        }
    }

    @Override
    public <T> T parseObject(String jsonStr, Class<T> clazz) {
        try {
            return objMapper.readValue(jsonStr, clazz);
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T parseObject(String jsonStr, TypeRef<T> typeRef) {
        try {
            JavaType type = objMapper.getTypeFactory()
                    .constructType(typeRef.getType());
            return objMapper.readValue(jsonStr, type);
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> List<T> parseList(String jsonStr, Class<T> elementClazz) {
        try {
            CollectionType collType = objMapper.getTypeFactory()
                    .constructCollectionType(List.class, elementClazz);
            return objMapper.readValue(jsonStr, collType);
        } catch (IOException e) {
            throw new SerializeException(e);
        }

    }

    @Override
    public <K,V> Map<K,V> parseMap(String jsonStr, Class<K> keyClazz, Class<V> valueClazz) {
        try {
            MapType mapType = objMapper.getTypeFactory()
                    .constructMapType(HashMap.class, keyClazz, valueClazz);
            return objMapper.readValue(jsonStr, mapType);
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
    public Jsons enable(SerializationFeature first, SerializationFeature... other) {
        objMapper.enable(first, other);
        return this;
    }

    public Jsons enable(Feature... feature) {
        objMapper.enable(feature);
        return this;
    }

    public Jsons disable(SerializationFeature first, SerializationFeature... other) {
        objMapper.disable(first, other);
        return this;
    }

    public Jsons disable(Feature... feature) {
        objMapper.disable(feature);
        return this;
    }

    /**
     * 启用日期按格式输出,默认格式yyyy-MM-dd HH:mm:ss
     * 
     * @return
     */
    @Override
    public void dateFormat() {
        objMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objMapper.setDateFormat(new SimpleDateFormat(DateHelper.YYYY_MM_DD_HH_mm_ss));
    }

    /**
     * 启用日期按格式输出,并指定日期格式
     * 
     * @param format
     */
    @Override
    public void dateFormat(String datePattern) {
        if (StringHelper.isEmpty(datePattern)) {
            dateFormat();
        } else {
            objMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objMapper.setDateFormat(new SimpleDateFormat(datePattern));
        }
    }

    @Override
    public void ignoreNull(boolean isIgnoreNull) {
        if (isIgnoreNull) {
            // 同一objMapper对象修改此配置不生效需要复制一个
            objMapper = objMapper.setDefaultPropertyInclusion(Include.NON_NULL)
                    .copy();
        } else {
            objMapper = objMapper.setDefaultPropertyInclusion(Include.ALWAYS)
                    .copy();
        }
    }

    @Override
    public void prettyPrint() {
        objMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

}
