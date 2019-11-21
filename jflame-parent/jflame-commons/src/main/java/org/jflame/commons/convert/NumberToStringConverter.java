package org.jflame.commons.convert;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class NumberToStringConverter extends ObjectToStringConverter<Number> {

    private NumberFormat format = null;

    public NumberToStringConverter() {
    }

    public NumberToStringConverter(NumberFormat format) {
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
