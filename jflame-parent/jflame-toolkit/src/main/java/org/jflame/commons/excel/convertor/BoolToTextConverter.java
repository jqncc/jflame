package org.jflame.commons.excel.convertor;

import org.jflame.commons.convert.ObjectToStringConverter;

public class BoolToTextConverter extends ObjectToStringConverter<Boolean> {

    @Override
    public String convert(Boolean source) {
        return source ? "是" : "否";
    }
}
