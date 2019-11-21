package org.jflame.commons.convert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;

import org.apache.commons.lang3.ArrayUtils;

import org.jflame.commons.exception.ConvertException;
import org.jflame.commons.util.DateHelper;
import org.jflame.commons.util.StringHelper;

/**
 * 字符串转新时间类型Temporal及子类,如:LocalDate,LocalDateTime,LocalTime等
 * 
 * @author yucan.zhang
 * @param <T> T extends Temporal
 */
public class StringToTemporalConverter<T extends Temporal> implements Converter<String,T> {

    private final Class<T> targetType;
    private DateTimeFormatter formatter;

    public StringToTemporalConverter(Class<T> targetType) {
        this.targetType = targetType;
    }

    public StringToTemporalConverter(Class<T> targetType, String pattern) {
        this.targetType = targetType;
        if (StringHelper.isNotEmpty(pattern)) {
            this.formatter = DateTimeFormatter.ofPattern(pattern);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T convert(String text) {
        if (StringHelper.isEmpty(text)) {
            return null;
        }
        try {
            if (formatter != null) {
                if (LocalDate.class == targetType) {
                    return (T) LocalDate.parse(text, formatter);
                } else if (LocalTime.class == targetType) {
                    return (T) LocalTime.parse(text, formatter);
                } else if (LocalDateTime.class == targetType) {
                    return (T) LocalDateTime.parse(text, formatter);
                } else if (ZonedDateTime.class == targetType) {
                    return (T) ZonedDateTime.parse(text, formatter);
                } else if (OffsetDateTime.class == targetType) {
                    return (T) OffsetDateTime.parse(text, formatter);
                } else if (OffsetTime.class == targetType) {
                    return (T) OffsetTime.parse(text, formatter);
                }
            } else {
                // 没有设置时间格式,尝试用常用或默认格式解析
                if (LocalDate.class == targetType) {
                    return (T) DateHelper.parseLocalDate(text, DateHelper.SHORT_PATTEN);
                } else if (LocalTime.class == targetType) {
                    return (T) DateHelper.parseLocalTime(text, DateHelper.TIME_PATTEN);
                } else if (LocalDateTime.class == targetType) {
                    return (T) DateHelper.parseLocalDateTime(text,
                            ArrayUtils.addAll(DateHelper.LONG_PATTEN, DateHelper.SHORT_PATTEN));
                } else if (ZonedDateTime.class == targetType) {
                    return (T) ZonedDateTime.parse(text);
                } else if (OffsetDateTime.class == targetType) {
                    return (T) OffsetDateTime.parse(text);
                } else if (OffsetTime.class == targetType) {
                    return (T) OffsetTime.parse(text);
                }
            }
        } catch (DateTimeParseException e) {
            throw new ConvertException(text + " 解析为时间失败 " + targetType);
        }
        throw new ConvertException("不支持的转换类型: " + targetType);
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

}
