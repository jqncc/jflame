package org.jflame.toolkit.util;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.Labels;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * json工具类，依赖于fastjson. 具体属性的序列化请使用@JSONField注解定制
 * 
 * @author yucan.zhang
 */
public final class JsonHelper {

    /**
     * Java对象序列化为JSON字符串
     * 
     * @param obj Java对象
     * @return json字符串
     */
    public static String toJson(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue);
    }

    /**
     * Java对象序列化为JSON字符串.使用LabelFilter定制属性<p>
     * 如：对象中@JSONField(label = "xx")注解定制.
     * 
     * @see com.alibaba.fastjson.serializer.LabelFilter
     * @param obj Java对象
     * @param includeLabels label分组名称，只包含指定label的属性
     * @return
     */
    public static String toJson(Object obj, String... includeLabels) {
        return JSON.toJSONString(obj, Labels.includes(includeLabels));
    }

    /**
     * Java对象序列化为JSON字符串,按UTF-8编码写入到OutputStream.
     * 
     * @param os OutputStream输出流
     * @param object Java对象
     * @return
     * @throws IOException
     */
    public static final int writeJSONString(OutputStream os, Object object) throws IOException {
        return JSON.writeJSONString(os, object, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 将JSON字符串反序列化为JavaBean
     * 
     * @param jsonStr json字符串
     * @param clazz JavaBean类型
     * @return
     */
    public static <T> T parseObject(String jsonStr, Class<T> clazz) {
        return JSON.parseObject(jsonStr, clazz);
    }

    /**
     * 将JSON字符串反序列化为java复杂对象
     * 
     * @param text JSON字符串
     * @param type TypeReference泛型表达
     * @param features
     * @see com.alibaba.fastjson.TypeReference
     * @return
     */
    public static <T> T parseObject(String text, TypeReference<T> type, Feature... features) {
        return JSON.parseObject(text, type, features);
    }

    /**
     * 将JSON字符串反序列化为泛型List
     * 
     * @param jsonStr json字符串
     * @param clazz List元素类型
     * @return
     */
    public static <T> List<T> parseArray(String jsonStr, Class<T> clazz) {

        return JSON.parseArray(jsonStr, clazz);
    }

    /**
     * 将JSON字符串反序列化为泛型Map
     * 
     * @param jsonStr json字符串
     * @param keyClazz Map键类型
     * @param valueClazz Map值类型
     * @return
     */
    public static <K,V> Map<K,V> parseMap(String jsonStr, Class<K> keyClazz, Class<V> valueClazz) {
        Type type = new TypeReference<Map<K,V>>() {
        }.getType();
        return JSON.parseObject(jsonStr, type);
    }

    // public static void main(String[] args) {
    // String aString="{\"name\":1}";
    // Map<String,Integer> m=parseMap(aString, String.class, Integer.class);
    // System.out.println(m.isEmpty());
    // }
}
