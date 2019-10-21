package org.jflame.toolkit.excel.convertor;

import org.jflame.toolkit.convert.ObjectToStringConverter;

public class BoolToTextConverter extends ObjectToStringConverter<Boolean> {

    @Override
    public String convert(Boolean source) {
        return source ? "是" : "否";
    }
}
