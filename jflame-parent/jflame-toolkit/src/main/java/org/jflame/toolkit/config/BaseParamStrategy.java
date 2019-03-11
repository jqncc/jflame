package org.jflame.toolkit.config;

import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.jflame.toolkit.util.NumberHelper;
import org.jflame.toolkit.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseParamStrategy implements ParamStrategy {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public final Boolean getBoolean(final ConfigKey<Boolean> configKey) {
        return getValue(configKey, new Parser<Boolean>() {

            public Boolean parse(final String value) {
                if (value == null) {
                    return null;
                }
                return BooleanUtils.toBoolean(value);
            }
        });
    }

    public final Boolean getBoolean(final String configKey) {
        return getBoolean(new ConfigKey<>(configKey));
    }

    public final Long getLong(final ConfigKey<Long> configKey) {
        return getValue(configKey, new Parser<Long>() {

            public Long parse(final String value) {
                return NumberHelper.parseLong(value);
            }
        });
    }

    public final Long getLong(final String configKey) {
        return getLong(new ConfigKey<>(configKey));
    }

    public final Integer getInt(final ConfigKey<Integer> configKey) {
        return getValue(configKey, new Parser<Integer>() {

            public Integer parse(final String value) {
                return NumberHelper.parseInt(value);
            }
        });
    }

    public final Integer getInt(final String configKey) {
        return getInt(new ConfigKey<>(configKey));
    }

    public final Double getDouble(ConfigKey<Double> configKey) {
        return getValue(configKey, new Parser<Double>() {

            public Double parse(final String value) {
                return NumberHelper.parseDouble(value);
            }
        });
    }

    public final Double getDouble(final String configKey) {
        return getDouble(new ConfigKey<>(configKey));
    }

    public final String getString(final ConfigKey<String> configKey) {
        return getValue(configKey, new Parser<String>() {

            public String parse(final String value) {
                return value;
            }
        });
    }

    public final String getString(final String configKey) {
        return getString(new ConfigKey<>(configKey));
    }

    public String[] getStringArray(ConfigKey<String[]> configKey) {
        return getValue(configKey, new Parser<String[]>() {

            @Override
            public String[] parse(String value) {
                if (StringHelper.isNotEmpty(value)) {
                    return StringHelper.split(value);
                }
                return null;
            }
        });
    }

    public <T> T getValue(final ConfigKey<T> configKey, final Parser<T> parser) {
        assertNotNull(configKey, "configKey cannot be null");
        final String value = getParam(configKey);
        T result = null;
        if (StringHelper.isNotEmpty(value)) {
            if (logger.isDebugEnabled()) {

                logger.debug("No value found for property {}, returning default {}", configKey.getName(),
                        configKey.getDefaultValue());
            }
            result = parser.parse(value.trim());
        }
        return Objects.isNull(result) ? configKey.getDefaultValue() : result;
    }

    @SuppressWarnings("rawtypes")
    protected abstract String getParam(final ConfigKey configKey);

    protected void assertNotNull(Object obj, String error) {
        if (Objects.isNull(obj)) {
            throw new IllegalArgumentException(error);
        }
    }

}