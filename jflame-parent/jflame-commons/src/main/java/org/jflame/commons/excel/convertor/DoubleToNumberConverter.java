package org.jflame.commons.excel.convertor;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.jflame.commons.convert.Converter;
import org.jflame.commons.exception.ConvertException;
import org.jflame.commons.util.MathHelper;

public class DoubleToNumberConverter<T extends Number> implements Converter<Double,T> {

    private final Class<T> numberClass;

    public DoubleToNumberConverter(Class<T> targetType) {
        this.numberClass = targetType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T convert(Double db) {
        if (numberClass == Integer.class || numberClass == int.class) {
            return (T) new Integer(db.intValue());
        } else if (numberClass == Short.class || numberClass == short.class) {
            return (T) new Short(db.shortValue());
        } else if (numberClass == Byte.class || numberClass == byte.class) {
            return (T) new Byte(db.byteValue());
        } else if (numberClass == Long.class || numberClass == long.class) {
            return (T) new Long(db.longValue());
        } else if (numberClass == Float.class || numberClass == float.class) {
            return (T) new Float(db.floatValue());
        } else if (numberClass == BigDecimal.class) {
            return (T) MathHelper.createBigDecimal(db);
        } else if (numberClass == BigInteger.class) {
            return (T) new BigInteger(db.toString());
        }
        throw new ConvertException("无法转换" + db + " 到 " + numberClass.getSimpleName());
    }
}
