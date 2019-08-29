package org.jflame.toolkit.excel.convertor;

import org.jflame.toolkit.convert.ObjectToTextConverter;

public class BoolToTextConverter extends ObjectToTextConverter<Boolean> {

    @Override
    public String convert(Boolean source) {
        return source ? "是" : "否";
    }
}
