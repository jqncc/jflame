package org.jflame.context.spring;

import java.lang.reflect.Method;

import org.springframework.cache.interceptor.KeyGenerator;

import org.jflame.commons.json.JsonHelper;
import org.jflame.commons.model.Chars;
import org.jflame.commons.reflect.BeanHelper;

/**
 * 缓存key生成
 * 
 * @author yucan.zhang
 */
public class CacheKeyGenerator implements KeyGenerator {

    private String keyPrefix;

    public CacheKeyGenerator() {
    }

    public CacheKeyGenerator(String _keyPrefix) {
        keyPrefix = _keyPrefix;
    }

    /**
     * 对指定方法签名生成key,生成规则:对象类型名:前缀.类名-方法名-参数(string/hashcode)
     * 
     * @param target 对象
     * @param method 方法
     * @param params 参数
     * @return
     */
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder strBuilder = new StringBuilder(30);
        strBuilder.append(getKeyPrefix());
        strBuilder.append('.');
        // 类名
        strBuilder.append(target.getClass()
                .getSimpleName());
        strBuilder.append(Chars.LINE);
        // 方法名
        strBuilder.append(method.getName());
        strBuilder.append(Chars.LINE);
        if (params.length > 0) {
            // 参数值
            for (Object object : params) {
                if (BeanHelper.isSimpleValueType(object.getClass())) {
                    strBuilder.append(object);
                } else {
                    strBuilder.append(JsonHelper.toJson(object)
                            .hashCode());
                }
            }
        } else {
            strBuilder.append(Chars.HEX_CHARS[0]);
        }
        return strBuilder.toString();
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    };

}
