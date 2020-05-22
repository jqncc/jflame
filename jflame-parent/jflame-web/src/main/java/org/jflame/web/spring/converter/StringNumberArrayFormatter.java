package org.jflame.web.spring.converter;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.format.Formatter;

import org.jflame.commons.convert.mutual.StringAndNumberArrayConverter;

/**
 * springmvc数字数组和英文逗号分隔的字符串类型转换器.
 * 
 * @author yucan.zhang
 */
public class StringNumberArrayFormatter implements Formatter<Number[]> {

    private StringAndNumberArrayConverter<? extends Number> converter;

    public StringNumberArrayFormatter(Class<? extends Number> fieldType) {
        converter = new StringAndNumberArrayConverter<>(fieldType);
    }

    @Override
    public String print(Number[] objs, Locale locale) {
        return converter.inverseConvert(objs);
    }

    @Override
    public Number[] parse(String text, Locale locale) throws ParseException {
        return converter.convert(text);
    }

}
