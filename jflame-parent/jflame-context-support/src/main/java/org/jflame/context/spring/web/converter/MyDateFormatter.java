package org.jflame.context.spring.web.converter;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.springframework.format.Formatter;

import org.jflame.toolkit.convert.mutual.StringAndDateConverter;

/**
 * springmvc日期类型转换器.支持java.util.Date及子类(如:java.util.Date,java.sql.Date,java.sql.Time,java.sql.Timestamp)转换
 * <p>
 * 内部转换由TextAndDateConverter实现,具体转换行为请查看该类说明
 * 
 * @see StringAndDateConverter
 * @author yucan.zhang
 */
public class MyDateFormatter<T extends Date> implements Formatter<T> {

    private StringAndDateConverter<T> converter;

    public MyDateFormatter(Class<T> dateClazz) {
        converter = new StringAndDateConverter<>(dateClazz);
    }

    public MyDateFormatter(Class<T> dateClazz, String datePatten) {
        converter = new StringAndDateConverter<>(dateClazz, datePatten);
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
