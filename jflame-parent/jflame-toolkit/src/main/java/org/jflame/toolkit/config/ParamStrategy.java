
package org.jflame.toolkit.config;

public interface ParamStrategy {

    Boolean getBoolean(ConfigKey<Boolean> ConfigKey);

    String getString(ConfigKey<String> ConfigKey);

    Long getLong(ConfigKey<Long> ConfigKey);

    Integer getInt(ConfigKey<Integer> ConfigKey);

    <T> T getValue(ConfigKey<T> ConfigKey,Parser<T> parser);
    
    public interface Parser<T> {
        T parse(String value);
    }

}
