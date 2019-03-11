package org.jflame.toolkit.config;

import javax.servlet.FilterConfig;

import org.jflame.toolkit.util.StringHelper;

/**
 * filter过滤器参数配置获取实现.
 * <p>
 * 如果在filter参数中未找到将向上查找ServletContext中参数
 * 
 * @author yucan.zhang
 */
public class FilterParamConfig extends BaseParamStrategy {

    protected FilterConfig filterConfig;

    public FilterParamConfig() {
    }

    public FilterParamConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected String getParam(ConfigKey configKey) {
        String value = filterConfig.getInitParameter(configKey.getName());
        if (StringHelper.isEmpty(value)) {
            value = filterConfig.getServletContext()
                    .getInitParameter(configKey.getName());
        }
        return value;
    }

}
