
package org.jflame.commons.config;

/**
 * 配置参数获取接口
 * 
 * @author yucan.zhang
 */
public interface ParamStrategy {

    /**
     * 获取配置参数布尔值,如果为null返回ConfigKey的缺省值
     * 
     * @param configKey
     * @return Boolean
     */
    Boolean getBoolean(ConfigKey<Boolean> configKey);

    /**
     * 获取配置参数string值,如果为null返回ConfigKey的缺省值
     * 
     * @param configKey
     * @return string
     */
    String getString(ConfigKey<String> configKey);

    /**
     * 获取配置参数Long值,如果为null返回ConfigKey的缺省值
     * 
     * @param configKey
     * @return Long
     */
    Long getLong(ConfigKey<Long> configKey);

    /**
     * 获取配置参数Integer值,如果为null返回ConfigKey的缺省值
     * 
     * @param configKey
     * @return Integer
     */
    Integer getInt(ConfigKey<Integer> configKey);

    /**
     * 获取配置参数Double值,如果为null返回ConfigKey的缺省值
     * 
     * @param configKey
     * @return Double
     */
    Double getDouble(ConfigKey<Double> configKey);

    /**
     * 获取配置参数值,值是以逗号分隔的字符串,转为字符串数组,如果为null返回ConfigKey的缺省值
     * 
     * @param configKey
     * @return String[]
     */
    String[] getStringArray(ConfigKey<String[]> configKey);

    /**
     * 获取配置参数,如果为null返回ConfigKey的缺省值
     * 
     * @param ConfigKey
     * @param parser
     * @return
     */
    <T> T getValue(ConfigKey<T> ConfigKey, Parser<T> parser);

    public interface Parser<T> {

        T parse(String value);
    }

}
