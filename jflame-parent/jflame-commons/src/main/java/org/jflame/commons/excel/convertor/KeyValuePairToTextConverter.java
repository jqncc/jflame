package org.jflame.commons.excel.convertor;

import org.jflame.commons.convert.ObjectToStringConverter;
import org.jflame.commons.model.pair.IIntKeyPair;

public class KeyValuePairToTextConverter extends ObjectToStringConverter<IIntKeyPair> {

    @Override
    public String convert(IIntKeyPair source) {
        return source.getValue();
    }
}
