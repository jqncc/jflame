package org.jflame.commons.convert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

import org.jflame.commons.util.DateHelper;
import org.jflame.commons.util.StringHelper;

/**
 * 新时间类型格式化为字符串.如:LocalDate,LocalDateTime,LocalTime
 * 
 * @author yucan.zhang
 */
public class TemporalToStringConverter extends ObjectToStringConverter<Temporal> {

    private DateTimeFormatter formatter;

    public TemporalToStringConverter() {
    }

    public TemporalToStringConverter(String pattern) {
        if (StringHelper.isNotEmpty(pattern)) {
            this.formatter = DateTimeFormatter.ofPattern(pattern);
        }
    }

    @Override
    public String convert(Temporal source) {
        if (source == null) {
            return null;
        }
        if (formatter == null) {
            if (source instanceof LocalDate) {
                return DateTimeFormatter.ofPattern(DateHelper.YYYY_MM_DD)
                        .format(source);
            } else if (source instanceof LocalDateTime) {
                return DateTimeFormatter.ofPattern(DateHelper.YYYY_MM_DD_HH_mm_ss)
                        .format(source);
            } else if (source instanceof LocalTime) {
                return DateTimeFormatter.ofPattern(DateHelper.HH_mm_ss)
                        .format(source);
            } else {
                return source.toString();
            }
        }
        return formatter.format(source);
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

}
