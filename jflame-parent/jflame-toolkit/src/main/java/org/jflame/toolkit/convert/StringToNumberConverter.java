package org.jflame.toolkit.convert;

import org.jflame.toolkit.util.NumberHelper;

/**
 * string转为Number及子类.
 * 
 * @author yucan.zhang
 * @param <T> T extends Number
 */
public class StringToNumberConverter<T extends Number> implements Converter<String,T> {

    private final Class<T> targetType;

    public StringToNumberConverter(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public T convert(String source) {
        if (source.isEmpty()) {
            return null;
        }
        return NumberHelper.parseNumber(source, this.targetType);
    }
}
