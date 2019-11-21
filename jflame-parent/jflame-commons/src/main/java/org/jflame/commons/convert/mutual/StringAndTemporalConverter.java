package org.jflame.commons.convert.mutual;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

import org.jflame.commons.convert.StringToTemporalConverter;
import org.jflame.commons.util.DateHelper;

/**
 * JDK1.8新时间类型Temporal及子类与字符串互转.
 * <p>
 * 1.该类继承{@link StringToTemporalConverter},所以字符串转为时间的行为与父类保持一至.<br>
 * 2.在未指定格式情况下,时间转为字符串默认格式如下:<br>
 * LocalDate -&gt; yyyy-MM-dd<br>
 * LocalTime -&gt; HH:mm:ss<br>
 * LocalDateTime -&gt; yyyy-MM-dd HH:mm:ss <br>
 * 其他类型使用JDK默认格式
 * 
 * @author yucan.zhang
 * @param <T>
 */
public class StringAndTemporalConverter<T extends Temporal> extends StringToTemporalConverter<T>
        implements MutualConverter<String,T> {

    public StringAndTemporalConverter(Class<T> targetType) {
        super(targetType);
    }

    public StringAndTemporalConverter(Class<T> targetType, String pattern) {
        super(targetType, pattern);
    }

    /**
     * 构造函数.只指定格式,日期类型默认为LocalDateTime
     * 
     * @param pattern
     */
    @SuppressWarnings("unchecked")
    public StringAndTemporalConverter(String pattern) {
        super((Class<T>) LocalDateTime.class, pattern);
    }

    @Override
    public String inverseConvert(T source) {
        if (source == null) {
            return null;
        }
        DateTimeFormatter formatter = getFormatter();
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

}
