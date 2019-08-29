package org.jflame.toolkit.convert;

import java.util.Date;

/**
 * string转为Date或子类.
 * <p>
 * 支持的类型包括:java.sql.Date,
 * 
 * @author yucan.zhang
 */
public final class TextToDateConverterFactory implements ConverterFactory<String,Date> {

    @Override
    public <T extends Date> Converter<String,T> getConverter(Class<T> targetType) {
        return new TextToDateConverter<T>(targetType);
    }

}
