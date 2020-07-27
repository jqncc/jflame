package org.jflame.commons.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.Labels;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;

import org.jflame.commons.model.TypeRef;
import org.jflame.commons.util.StringHelper;

/**
 * 基于fastjson实现json工具类
 * 
 * @author yucan.zhang
 */
public class Fastjsons implements Jsons {

    private SerializerFeature[] serializerFeatures;
    private Feature[] parseFeatures;
    private String datePattern;

    public Fastjsons() {
        this(false);
    }

    public Fastjsons(boolean enableAutoTypeSupport) {
        if (enableAutoTypeSupport) {
            serializerFeatures = new SerializerFeature[] { SerializerFeature.IgnoreNonFieldGetter,
                    SerializerFeature.IgnoreErrorGetter,SerializerFeature.DisableCircularReferenceDetect,
                    SerializerFeature.WriteClassName };
            parseFeatures = new Feature[] { Feature.IgnoreNotMatch,Feature.AllowSingleQuotes,Feature.SupportAutoType };
        } else {
            serializerFeatures = new SerializerFeature[] { SerializerFeature.IgnoreNonFieldGetter,
                    SerializerFeature.IgnoreErrorGetter,SerializerFeature.DisableCircularReferenceDetect,
                    SerializerFeature.WriteMapNullValue };
            parseFeatures = new Feature[] { Feature.IgnoreNotMatch,Feature.AllowSingleQuotes };
        }

    }

    @Override
    public String toJson(Object obj) {
        return toJson(obj, (SerializeFilter) null);
    }

    public String toJson(Object obj, SerializeFilter... filters) {
        SerializeWriter out = new SerializeWriter(null, JSON.DEFAULT_GENERATE_FEATURE, serializerFeatures);
        try {
            JSONSerializer serializer = new JSONSerializer(out, SerializeConfig.globalInstance);
            if (datePattern != null) {
                serializer.setDateFormat(datePattern);
                serializer.config(SerializerFeature.WriteDateUseDateFormat, true);
            }

            if (filters != null) {
                for (SerializeFilter filter : filters) {
                    serializer.addFilter(filter);
                }
            }
            serializer.write(obj);
            return out.toString();
        } finally {
            out.close();
        }
    }

    /**
     * Java对象序列化为JSON字符串,按属性组过滤.
     * <p>
     * 使用LabelFilter定制属性. 如：对象中@JSONField(label = "xx")注解定制.
     * 
     * @see com.alibaba.fastjson.serializer.LabelFilter
     * @param obj Java对象
     * @param includeLabels label分组名称，只包含指定label的属性
     * @return
     */
    public String toJsonView(Object obj, String... includeLabels) {
        return JSON.toJSONString(obj, Labels.includes(includeLabels));
    }

    @Override
    public String toJsonFilter(Object obj, boolean isInclude, String[] properties) {
        if (isInclude) {
            return toJsonIncludeField(obj, properties);
        } else {
            return toJsonExcludeField(obj, properties);
        }
    }

    /**
     * Java对象序列化为JSON字符串,指定要排除的属性.
     * 
     * @param obj Java对象
     * @param excludeFields 要排除的属性
     * @return
     */
    public String toJsonExcludeField(Object obj, String... excludeFields) {
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
        for (String field : excludeFields) {
            filter.getExcludes()
                    .add(field);
        }
        return JSON.toJSONString(obj, filter);
    }

    /**
     * Java对象序列化为JSON字符串,指定要包含的属性.
     * 
     * @param obj Java对象
     * @param includeFields 要包含的属性
     * @return
     */
    public String toJsonIncludeField(Object obj, String... includeFields) {
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(includeFields);
        return JSON.toJSONString(obj, filter);
    }

    @Override
    public byte[] toJsonBytes(Object obj) {
        SerializeWriter out = new SerializeWriter(null, JSON.DEFAULT_GENERATE_FEATURE, serializerFeatures);
        try {
            JSONSerializer serializer = new JSONSerializer(out, SerializeConfig.globalInstance);
            if (datePattern != null) {
                serializer.setDateFormat(datePattern);
                serializer.config(SerializerFeature.WriteDateUseDateFormat, true);
            }
            serializer.write(obj);
            return out.toBytes(StandardCharsets.UTF_8);
        } finally {
            out.close();
        }
    }

    @Override
    public void writeJSONString(OutputStream os, Object object) throws IOException {
        JSON.writeJSONString(os, object, serializerFeatures);
    }

    @Override
    public <T> T parseObject(String jsonStr, Class<T> clazz) {
        return JSON.parseObject(jsonStr, clazz, parseFeatures);
    }

    @Override
    public <T> T parseObject(String jsonStr, TypeRef<T> typeRef) {
        return JSON.parseObject(jsonStr, typeRef.getType(), parseFeatures);
    }

    @Override
    public <T> List<T> parseList(String jsonStr, Class<T> elementClazz) {
        return JSON.parseArray(jsonStr, elementClazz);
    }

    @Override
    public <K,V> Map<K,V> parseMap(String jsonStr, Class<K> keyClazz, Class<V> valueClazz) {
        Type type = new TypeReference<Map<K,V>>() {
        }.getType();
        return JSON.parseObject(jsonStr, type, parseFeatures);
    }

    @Override
    public void dateFormat() {
        serializerFeatures = ArrayUtils.add(serializerFeatures, SerializerFeature.WriteDateUseDateFormat);
    }

    @Override
    public void dateFormat(String datePattern) {
        dateFormat();
        if (StringHelper.isEmpty(datePattern)) {
            this.datePattern = datePattern;
        }
    }

    @Override
    public void ignoreNull(boolean isIgnoreNull) {
        if (isIgnoreNull) {
            if (ArrayUtils.contains(serializerFeatures, SerializerFeature.WriteMapNullValue)) {
                serializerFeatures = ArrayUtils.removeElement(serializerFeatures, SerializerFeature.WriteMapNullValue);
            }
        } else {
            if (!ArrayUtils.contains(serializerFeatures, SerializerFeature.WriteMapNullValue)) {
                serializerFeatures = ArrayUtils.add(serializerFeatures, SerializerFeature.WriteMapNullValue);
            }
        }
    }

    @Override
    public void prettyPrint() {
        if (!ArrayUtils.contains(serializerFeatures, SerializerFeature.PrettyFormat)) {
            serializerFeatures = ArrayUtils.add(serializerFeatures, SerializerFeature.PrettyFormat);
        }
    }

    public JSONObject parse(String jsonStr) {
        return JSON.parseObject(jsonStr);
    }

}
