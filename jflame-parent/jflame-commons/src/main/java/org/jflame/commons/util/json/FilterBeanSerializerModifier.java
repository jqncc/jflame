package org.jflame.commons.util.json;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import org.jflame.commons.util.ArrayHelper;

/**
 * 动态指定要过滤掉的属性.
 * 
 * @author yucan.zhang
 */
public class FilterBeanSerializerModifier extends BeanSerializerModifier {

    private String[] filterProperties;
    private boolean isInclude;

    public FilterBeanSerializerModifier(String[] properties) {
        this(properties, true);
    }

    public FilterBeanSerializerModifier(String[] properties, boolean isInclude) {
        this.filterProperties = properties;
        this.isInclude = isInclude;
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
            List<BeanPropertyWriter> beanProperties) {
        if (ArrayHelper.isNotEmpty(filterProperties)) {
            if (isInclude) {
                beanProperties.removeIf(p -> {
                    return !ArrayUtils.contains(filterProperties, p.getName());
                });
            } else {
                beanProperties.removeIf(p -> {
                    return ArrayUtils.contains(filterProperties, p.getName());
                });
            }
        }

        return beanProperties;
    }
}
