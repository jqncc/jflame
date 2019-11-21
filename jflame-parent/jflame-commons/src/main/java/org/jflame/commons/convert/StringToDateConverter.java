package org.jflame.commons.convert;

import java.util.Date;
import java.util.Optional;

import org.jflame.commons.util.DateHelper;

/**
 * 字符串解析为时间<code>java.util.Date</code>或它的子类java.sql.Date,java.sql.Time,java.sql.Timestamp
 * <p>
 * 如果不指定格式,将尝试使用多种常用格式解析,具体请查看 {@link DateHelper#parseDate(String, Optional, Class)}
 * 
 * @author yucan.zhang
 * @param <T> T extends Date
 */
@SuppressWarnings("unchecked")
public class StringToDateConverter<T extends Date> implements Converter<String,T> {

    private final Class<T> targetType;
    private Optional<String> pattern;

    /**
     * 构造函数,默认解析为java.util.Date
     */
    public StringToDateConverter() {
        this((Class<T>) java.util.Date.class, null);
    }

    public StringToDateConverter(String pattern) {
        this((Class<T>) java.util.Date.class, pattern);
    }

    /**
     * 构造函数,指定解析的时间类型
     * 
     * @param targetType 要解析的时间类型
     */
    public StringToDateConverter(Class<T> targetType) {
        this(targetType, null);
    }

    public StringToDateConverter(Class<T> targetType, String pattern) {
        this.targetType = targetType;
        this.pattern = Optional.ofNullable(pattern);
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

    public Optional<String> getPattern() {
        return pattern;
    }

}
