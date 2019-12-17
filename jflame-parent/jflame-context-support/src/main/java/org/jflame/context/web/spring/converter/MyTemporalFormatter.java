package org.jflame.context.web.spring.converter;

import java.text.ParseException;
import java.time.temporal.Temporal;
import java.util.Locale;

import org.springframework.format.Formatter;

import org.jflame.commons.convert.mutual.StringAndTemporalConverter;
import org.jflame.commons.util.DateHelper;

/**
 * springmvcJDK1.8新时间类型转换器,支持Temporal子类(如:LocalDateTime,LocalDate,LocalTime)转换
 * <p>
 * 未指定时间格式时默认支持格式请查看:<br>
 * {@link DateHelper#LONG_PATTEN}, {@link DateHelper#SHORT_PATTEN} , {@link DateHelper#TIME_PATTEN}
 * 
 * @author yucan.zhang
 */
public class MyTemporalFormatter<T extends Temporal> implements Formatter<T> {

    private StringAndTemporalConverter<T> converter;

    public MyTemporalFormatter(Class<T> dateClazz) {
        converter = new StringAndTemporalConverter<>(dateClazz);
    }

    public MyTemporalFormatter(Class<T> dateClazz, String datePatten) {
        converter = new StringAndTemporalConverter<>(dateClazz, datePatten);
    }

    @Override
    public String print(T object, Locale locale) {
        return converter.inverseConvert(object);
    }

    @Override
    public T parse(String text, Locale locale) throws ParseException {
        return converter.convert(text);
    }

}
