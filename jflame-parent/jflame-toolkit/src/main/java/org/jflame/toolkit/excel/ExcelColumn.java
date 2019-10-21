package org.jflame.toolkit.excel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jflame.toolkit.convert.Converter;
import org.jflame.toolkit.convert.ObjectToStringConverter;
import org.jflame.toolkit.excel.handler.NullConverter;

/**
 * excel注解,用于实体类数据转为excel文件.
 * 
 * @author zyc
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD,ElementType.FIELD })
public @interface ExcelColumn {

    /**
     * 列名
     * 
     * @return
     */
    public String name();

    /**
     * 顺序
     * 
     * @return
     */
    public int order();

    /**
     * 列宽
     * 
     * @return
     */
    public int width() default 256 * 20;

    /**
     * 格式,只有时间或数字类型支持
     * 
     * @return
     */
    public String fmt() default "";

    @SuppressWarnings("rawtypes")
    public Class<? extends ObjectToStringConverter> writeConverter() default NullConverter.class;

    @SuppressWarnings("rawtypes")
    public Class<? extends Converter> readConverter() default NullConverter.class;

}
