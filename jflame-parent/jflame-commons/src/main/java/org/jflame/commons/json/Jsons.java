package org.jflame.commons.json;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.jflame.commons.model.TypeRef;

public interface Jsons {

    /**
     * 对象序列化为json字符串
     * 
     * @param obj 待转换对象
     * @return
     */
    String toJson(Object obj);

    /**
     * 对象序列化为json byte[]
     * 
     * @param obj 待转换对象
     * @return
     */
    byte[] toJsonBytes(Object obj);

    /**
     * Java对象序列化为JSON字符串,指定过滤属性,支持包含或排除模式
     * 
     * @param obj 要序列化的Java对象
     * @param isInclude true=包含属性,false=排除属性
     * @param properties 要过滤的属性
     * @return
     */
    public String toJsonFilter(Object obj, boolean isInclude, String[] properties);

    /**
     * 对象转为序列化为json后输出到流os
     * 
     * @param os 输出流
     * @param object 待转换对象
     * @throws IOException
     */
    void writeJSONString(OutputStream os, Object object) throws IOException;

    /**
     * 反序列化json字符串为指定类型的java对象
     * 
     * @param jsonStr json字符串
     * @param clazz 目标对象类型(非嵌套泛型Class)
     * @return
     */
    <T> T parseObject(String jsonStr, Class<T> clazz);

    /**
     * 反序列化json字符串为指定类型的java对象,适用复杂类型
     * 
     * @param jsonStr json字符串
     * @param typeRef 结果泛型装载类,TypeRef
     * @return
     */
    <T> T parseObject(String jsonStr, TypeRef<T> typeRef);

    /**
     * 反序列化json字符串为集合List
     * 
     * @param jsonStr json字符串
     * @param elementClazz 目标集合元素类型
     * @return
     */
    <T> List<T> parseList(String jsonStr, Class<T> elementClazz);

    /**
     * 反序列化json字符串为Map
     * 
     * @param jsonStr json字符串
     * @param keyClazz map key 类型
     * @param valueClazz map value类型
     * @return
     */
    <K,V> Map<K,V> parseMap(String jsonStr, Class<K> keyClazz, Class<V> valueClazz);

    /**
     * 启用日期按格式输出,默认格式yyyy-MM-dd HH:mm:ss
     */
    void dateFormat();

    /**
     * 启用日期按格式输出,并指定日期格式
     * 
     * @param datePattern
     */
    void dateFormat(String datePattern);

    /**
     * 设置是否忽略值为null的属性
     * 
     * @param isIgnoreNull true=忽略空值
     */
    void ignoreNull(boolean isIgnoreNull);

    /**
     * 设置格式化输出json
     */
    void prettyPrint();

}