package org.jflame.toolkit.config;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class PropertiesConfigHolder {

    private static PropertiesConfig config;
    private static AtomicBoolean isLoad = new AtomicBoolean(false);

    public static void loadConfig(String... propertiesFils) {
        if (!isLoad.get()) {
            config = new PropertiesConfig(propertiesFils);
            isLoad.set(true);
        }
    }

    public static void loadConfig(Properties props) {
        if (!isLoad.get()) {
            config = new PropertiesConfig(props);
            isLoad.set(true);
        }
    }

    public static String getString(final ConfigKey<String> configKey) {
        return config.getParam(configKey);
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
