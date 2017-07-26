package org.jflame.web.config;

import javax.servlet.ServletConfig;

import org.jflame.toolkit.util.StringHelper;

public class ServletParamConfig extends BaseParamStrategy {

    private ServletConfig servletConfig;

    public ServletParamConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected String getParam(ConfigKey configKey) {
        return servletConfig.getInitParameter(configKey.getName());
    }
    
    public String[] getStringArray(ConfigKey<String[]> configKey) {
        return getValue(configKey,new Parser<String[]>() {
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
