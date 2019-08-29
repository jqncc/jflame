package org.jflame.toolkit.convert;

import java.util.Date;
import java.util.Optional;

import org.jflame.toolkit.util.DateHelper;

/**
 * 字符串解析为时间<code>java.util.Date</code>或它的子类java.sql.Date,java.sql.Time,java.sql.Timestamp
 * 
 * @author yucan.zhang
 * @param <T>
 */
public class TextToDateConverter<T extends Date> implements Converter<String,T> {

    private final Class<T> targetType;
    private Optional<String> pattern;

    /**
     * 构造函数,默认解析为java.util.Date
     */
    @SuppressWarnings("unchecked")
    public TextToDateConverter() {
        targetType = (Class<T>) java.util.Date.class;
        this.pattern = Optional.empty();
    }

    /**
     * 构造函数,指定解析的时间类型
     * 
     * @param targetType 要解析的时间类型
     */
    public TextToDateConverter(Class<T> targetType) {
        this.targetType = targetType;
        this.pattern = Optional.empty();
    }

    public TextToDateConverter(Class<T> targetType, Optional<String> pattern) {
        this.targetType = targetType;
        this.pattern = pattern;
    }

    @Override
    public T convert(String source) {
        if (source.isEmpty()) {
            return null;
        }
        return DateHelper.parseDate(source, pattern, targetType);
    }

    public void setPattern(Optional<String> pattern) {
        this.pattern = pattern;
    }

    public TextToDateConverter<T> pattern(String pattern) {
        this.pattern = Optional.ofNullable(pattern);
        return this;
    }

}
