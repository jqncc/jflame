package org.jflame.context.web.spring.converter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

/**
 * 数组与逗号分隔的字符串间的转换器工厂.实现如下类型互相转换:<br>
 * <p>
 * String[] x=new String[]{"a","2","c"} &lt;=&gt; "a,2,c" //string数组<br>
 * Integer[] x=new Integer[]{1,4,3} &lt;=&gt; "1,4,3" //Integer数组 <br>
 * Long[] x=new Long[]{100L,4L,302L} &lt;=&gt; "100,4,302" //Long数组
 * </p>
 * 
 * @author yucan.zhang
 */
public class ArrayJoinTextFormatterFactory implements AnnotationFormatterFactory<ArrayJoinText> {

    private final Set<Class<?>> fieldTypes;
    private final StringArrayFormatter strArrayFormatter;

    public ArrayJoinTextFormatterFactory() {
        fieldTypes = new HashSet<>();
        fieldTypes.add(String[].class);
        fieldTypes.add(Integer[].class);
        fieldTypes.add(Long[].class);
        fieldTypes.add(BigDecimal[].class);
        fieldTypes.add(Double[].class);
        fieldTypes.add(Short[].class);
        fieldTypes.add(Float[].class);
        fieldTypes.add(Byte[].class);
        fieldTypes.add(Number[].class);

        strArrayFormatter = new StringArrayFormatter();
    }

    @Override
    public Set<Class<?>> getFieldTypes() {
        return fieldTypes;
    }

    @Override
    public Printer<?> getPrinter(ArrayJoinText annotation, Class<?> fieldType) {
        return strArrayFormatter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Parser<?> getParser(ArrayJoinText annotation, Class<?> fieldType) {
        if (Number.class.isAssignableFrom(fieldType)) {
            return new StringNumberArrayFormatter((Class<Number>) fieldType);
        } else {
            return strArrayFormatter;
        }
    }

}
