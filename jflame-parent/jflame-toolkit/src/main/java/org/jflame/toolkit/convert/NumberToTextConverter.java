package org.jflame.toolkit.convert;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class NumberToTextConverter extends ObjectToTextConverter<Number> {

    private NumberFormat format = null;

    public NumberToTextConverter() {
    }

    public NumberToTextConverter(NumberFormat format) {
        this.format = format;
    }

    @Override
    public String convert(Number source) {
        if (source == null) {
            return null;
        }
        if (format != null) {
            return format.format(source);
        } else {
            if (source instanceof BigDecimal) {
                return ((BigDecimal) source).toPlainString();
            }
            return String.valueOf(source);
        }
    }

}
