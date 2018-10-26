package org.jflame.web.config;

import javax.servlet.FilterConfig;

import org.jflame.toolkit.util.StringHelper;

public class FilterParamConfig extends BaseParamStrategy {

    private FilterConfig filterConfig;

    public FilterParamConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected String getParam(ConfigKey configKey) {
        return filterConfig.getInitParameter(configKey.getName());
    }

    public String[] getStringArray(ConfigKey<String[]> configKey) {
        return getValue(configKey, new Parser<String[]>() {
            @Override
            public String[] parse(String value) {
                if (StringHelper.isNotEmpty(value)) {
                    return StringHelper.split(value.trim());
                }
                return null;
            }
        });
    }
}
