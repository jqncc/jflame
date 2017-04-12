package org.jflame.toolkit.keygenerate;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 缓存key生成
 * 
 * @author yucan.zhang
 */
public class CacheKeyGenerator {

    private final static int NO_PARAM_KEY = 0;
    private String keyPrefix = "jflame";// key前缀，用于区分不同项目的缓存，建议每个项目单独设置

    /**
     * 对指定方法签名生成key,生成规则:对象类型名:前缀:方法名:参数hashcode
     * 
     * @param target 对象
     * @param method 方法
     * @param params 参数
     * @return
     */
    public Object generate(Object target, Method method, Object... params) {
        char sp = ':';
        StringBuilder strBuilder = new StringBuilder(20);
        strBuilder.append(keyPrefix);
        strBuilder.append(sp);
        // 类名
        strBuilder.append(target.getClass().getSimpleName());
        strBuilder.append(sp);
        // 方法名
        strBuilder.append(method.getName());
        strBuilder.append(sp);
        if (params.length > 0) {
            // 参数值
            strBuilder.append(Arrays.hashCode(params));
        } else {
            strBuilder.append(NO_PARAM_KEY);
        }
        return strBuilder.toString();
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

}
