package org.jflame.toolkit.excel.handler;

import org.jflame.toolkit.convert.ObjectToTextConverter;

/**
 * 空转换器,啥事不干直接返回null,作为注解ExcelCoumn的转换器属性的默认值用
 * 
 * @author yucan.zhang
 */
public class NullConverter extends ObjectToTextConverter<Object> {

    @Override
    public String convert(Object source) {
        return null;
    }
}
