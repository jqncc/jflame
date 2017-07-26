package org.jflame.web.config;

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
                return BooleanUtils.toBoolean(value);
            }
        });
    }

    public final Long getLong(final ConfigKey<Long> configKey) {
        return getValue(configKey, new Parser<Long>() {
            public Long parse(final String value) {
                return NumberHelper.parseLong(value);
            }
        });
    }

    public final Integer getInt(final ConfigKey<Integer> configKey) {
        return getValue(configKey, new Parser<Integer>() {

            public Integer parse(final String value) {
                return NumberHelper.parseInt(value);
            }
        });
    }

    public final String getString(final ConfigKey<String> configKey) {
        return getValue(configKey, new Parser<String>() {
            public String parse(final String value) {
                return value;
            }
        });
    }


    public <T> T getValue(final ConfigKey<T> configKey, final Parser<T> parser) {
        assertNotNull(configKey, "configKey cannot be null");
        final String value = getParam(configKey);
        T result=null;
        if (StringHelper.isNotEmpty(value)) {
            logger.trace("No value found for property {}, returning default {}", configKey.getName(), configKey.getDefaultValue());
            result=parser.parse(value);
        }
        return result==null?configKey.getDefaultValue():result;
    }

    @SuppressWarnings("rawtypes")
    protected abstract String getParam(final ConfigKey configKey);

    protected void assertNotNull(Object obj,String error) {
        if (obj==null) {
            throw new IllegalArgumentException(error);
        }
    }


}
