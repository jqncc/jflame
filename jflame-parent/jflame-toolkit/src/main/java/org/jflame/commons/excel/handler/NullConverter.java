package org.jflame.commons.excel.handler;

import org.jflame.commons.convert.ObjectToStringConverter;

/**
 * 空转换器,啥事不干直接返回null,作为注解ExcelCoumn的转换器属性的默认值用
 * 
 * @author yucan.zhang
 */
public class NullConverter extends ObjectToStringConverter<Object> {

    @Override
    public String convert(Object source) {
        return null;
    }
}
