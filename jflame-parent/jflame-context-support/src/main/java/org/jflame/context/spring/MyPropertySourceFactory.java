package org.jflame.context.spring;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.io.support.ResourcePropertySource;

import org.jflame.commons.config.PropertiesConfigHolder;

/**
 * 自定义spring PropertySourceFactory.实现spring加载配置文件同时,将配置注入到PropertiesConfigHolder
 * 
 * @see MyPropertyPlaceholderConfigurer
 * @author yucan.zhang
 */
public class MyPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        PropertySource<?> propSource = (name != null ? new ResourcePropertySource(name, resource)
                : new ResourcePropertySource(resource));
        if (propSource.getSource() instanceof Properties) {
            PropertiesConfigHolder.loadProperties((Properties) propSource.getSource());
        }
        return propSource;
    }

}
