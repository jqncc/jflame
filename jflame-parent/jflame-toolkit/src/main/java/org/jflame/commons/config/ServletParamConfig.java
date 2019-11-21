package org.jflame.commons.config;

import javax.servlet.ServletConfig;

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

}
