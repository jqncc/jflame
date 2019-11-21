package org.jflame.commons.config;

import java.util.Properties;

/**
 * 加载properties文件存为静态资源,使用静态方法获取配置属性
 * 
 * @author yucan.zhang
 */
public abstract class PropertiesConfigHolder {

    private static PropertiesConfig config;

    public synchronized static void loadProperties(String... propertiesFils) {
        config = new PropertiesConfig(propertiesFils);
    }

    public synchronized static void loadProperties(Properties props) {
        config = new PropertiesConfig(props);
    }

    public static String getString(final ConfigKey<String> configKey) {
        return config.getString(configKey);
    }

    public static String getString(String configKey) {
        return config.getString(configKey);
    }

    public static Integer getInt(String configKey) {
        return config.getInt(configKey);
    }

    public static Integer getInt(final ConfigKey<Integer> configKey) {
        return config.getInt(configKey);
    }

    public static Boolean getBoolean(String configKey) {
        return config.getBoolean(configKey);
    }

    public static Boolean getBoolean(final ConfigKey<Boolean> configKey) {
        return config.getBoolean(configKey);
    }

    public static Long getLong(String paramKey) {
        return config.getLong(paramKey);
    }

    public static Long getLong(final ConfigKey<Long> configKey) {
        return config.getLong(configKey);
    }

    public static Double getDouble(String configKey) {
        return config.getDouble(configKey);
    }

    public static Double getDouble(final ConfigKey<Double> configKey) {
        return getDouble(configKey);
    }

    public static String[] getStringArray(ConfigKey<String[]> configKey) {
        return config.getStringArray(configKey);
    }

    public static String[] getStringArray(String configKey) {
        return config.getStringArray(new ConfigKey<String[]>(configKey));
    }

    public static PropertiesConfig getConfig() {
        return config;
    }

}
