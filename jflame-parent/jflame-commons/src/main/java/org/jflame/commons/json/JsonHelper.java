package org.jflame.commons.json;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.TypeRef;
import org.jflame.commons.reflect.SpiFactory;

/**
 * 提供json常用操作静态方法工具类.
 * <p>
 * 内部使用{@link Jsons}接口实现类完成json操作,默认使用Fastjsons.<br>
 * 如果想切换为其他json组件可通过SPI机制实现. 如切换为Jacksons,在项目META-INF/services目录下新建名称为org.jflame.commons.util.json.Jsons的文件,文件内容:org.
 * jflame.commons.util.json.Jsons.Jacksons
 * 
 * @see org.jflame.commons.json.Jsons
 * @author yucan.zhang
 */
public final class JsonHelper {

    private static Jsons jsonClient;

    static {
        jsonClient = SpiFactory.getBean(Jsons.class, Jacksons.class);
    }

    /**
     * Java对象序列化为JSON字符串
     * 
     * @param obj Java对象
     * @return json字符串
     */
    public static String toJson(Object obj) {
        return jsonClient.toJson(obj);
    }

    /**
     * Java对象序列化为json byte[]
     * 
     * @param obj Java对象
     * @return json byte[]
     */
    public static byte[] toJsonBytes(Object obj) {
        return jsonClient.toJsonBytes(obj);
    }

    /**
     * Java对象序列化为JSON字符串,按UTF-8编码写入到OutputStream.
     * 
     * @param os OutputStream输出流
     * @param object Java对象
     * @throws IOException
     */
    public static void writeJSONString(OutputStream os, Object object) throws IOException {
        jsonClient.writeJSONString(os, object);
    }

    /**
     * Java对象序列化为JSON字符串,动态指定要序列化的属性.使用jackson时对象上不能有JsonFilter注解
     * 
     * @param obj Java对象
     * @param includes 要包含的属性名称数组
     * @return
     */
    public static String toJsonInclude(Object obj, String[] includes) {
        return jsonClient.toJsonFilter(obj, true, includes);
    }

    /**
     * Java对象序列化为JSON字符串,动态排除要序列化的属性.使用jackson时对象上不能有JsonFilter注解
     * 
     * @param obj Java对象
     * @param excludes 要排除的属性名称数组
     * @return
     */
    public static String toJsonExclude(Object obj, String[] excludes) {
        return jsonClient.toJsonFilter(obj, false, excludes);
    }

    /**
     * JSON字符串解析为Java对象(适用于非泛型对象)
     * 
     * @param jsonStr json字符串
     * @param clazz 目标Java类型
     * @return
     */
    public static <T> T parseObject(String jsonStr, Class<T> clazz) {
        return jsonClient.parseObject(jsonStr, clazz);
    }

    /**
     * JSON字符串解析为java对象(适合于复杂泛型对象)
     * 
     * @see TypeRef
     * @param text JSON字符串
     * @param type TypeRef泛型表达
     * @return
     */
    public static <T> T parseObject(String text, TypeRef<T> type) {
        return jsonClient.parseObject(text, type);
    }

    /**
     * 将JSON字符串解析为泛型List
     * 
     * @param jsonStr json字符串
     * @param elementClazz List元素类型
     * @return
     */
    public static <T> List<T> parseList(String jsonStr, Class<T> elementClazz) {
        return jsonClient.parseList(jsonStr, elementClazz);
    }

    /**
     * JSON字符串解析为Map
     * 
     * @param jsonStr json字符串
     * @param keyClazz Map键类型
     * @param valueClazz Map值类型
     * @return
     */
    public static <K,V> Map<K,V> parseMap(String jsonStr, Class<K> keyClazz, Class<V> valueClazz) {
        return jsonClient.parseMap(jsonStr, keyClazz, valueClazz);
    }

    /**
     * JSON字符串解析为 Map&lt;String,String&gt;
     * 
     * @param jsonStr json字符串
     * @return Map&lt;String,String&gt;
     */
    public static Map<String,String> parseMap(String jsonStr) {
        return jsonClient.parseMap(jsonStr, String.class, String.class);
    }

    /**
     * JSON字符串解析为CallResult
     * 
     * @param jsonStr json字符串
     * @param dataClazz CallResult.data类型
     * @return
     */
    public static <T> CallResult<T> parseCallResult(String jsonStr, Class<T> dataClazz) {
        return jsonClient.parseObject(jsonStr, new TypeRef<CallResult<T>>() {
        });
    }

    /**
     * JSON字符串解析为转为Json Node对象,使用fastjson时是JSONObject,使用Jackson时是JsonNode
     * 
     * @param jsonStr
     * @return
     */
    public static Object parseNode(String jsonStr) {
        return jsonClient.parseNode(jsonStr);
    }
}
