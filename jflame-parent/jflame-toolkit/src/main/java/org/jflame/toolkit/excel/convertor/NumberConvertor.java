package org.jflame.toolkit.excel.convertor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.util.StringHelper;

/**
 * excel值与double型转换器
 * 
 * @author yucan.zhang
 */
public class NumberConvertor implements ICellValueConvertor<Number> {

    private Class<? extends Number> numberClass;

    public NumberConvertor(Class<? extends Number> realClass) {
        this.numberClass = realClass;
    }

    @Override
    public Number convertFromExcel(Object cellValue, final String fmt) throws ConvertException {
        if (cellValue == null) {
            return null;
        }
        if ((cellValue instanceof Double)) {
            return (Double) cellValue;
        }
        String text = StringUtils.trimToEmpty(cellValue.toString());
        try {
            if (StringUtils.isNotEmpty(fmt)) {
                DecimalFormat formator = new DecimalFormat();
                formator.setParseBigDecimal(true);
                formator.applyPattern(fmt);
                formator.setGroupingUsed(false);

                Number number = formator.parse(text);
                return convertNumberToTargetClass(number, this.numberClass);
            } else {
                return parseNumber(text, numberClass);
            }
        } catch (ParseException e) {
            throw new ConvertException("使用格式:" + fmt + "转换失败", e);
        }
    }

    @Override
    public String convertToExcel(final Number value, final String fmt) throws ConvertException {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        DecimalFormat formator = new DecimalFormat();
        formator.setGroupingUsed(false);
        try {
            if (StringHelper.isNotEmpty(fmt)) {
                formator.applyPattern(fmt);
            }
            return formator.format(value);
        } catch (IllegalArgumentException e) {
            throw new ConvertException("使用格式:" + fmt + "转换失败", e);
        }
    }

    @Override
    public String getConvertorName() {
        return CellConvertorEnum.number.name();
    }

    @SuppressWarnings("unchecked")
    <T extends Number> T convertNumberToTargetClass(Number number, Class<T> targetClass)
            throws IllegalArgumentException {

        if (targetClass.isInstance(number)) {
            return (T) number;
        } else if (Byte.class == targetClass) {
            long value = number.longValue();
            if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                throw new IllegalArgumentException("值" + number + "超出范围" + targetClass.getName());
            }
            return (T) Byte.valueOf(number.byteValue());
        } else if (Short.class == targetClass) {
            long value = number.longValue();
            if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                throw new IllegalArgumentException("值" + number + "超出范围" + targetClass.getName());
            }
            return (T) Short.valueOf(number.shortValue());
        } else if (Integer.class == targetClass) {
            long value = number.longValue();
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("值" + number + "超出范围" + targetClass.getName());
            }
            return (T) Integer.valueOf(number.intValue());
        } else if (Long.class == targetClass) {
            BigInteger bigInt = null;
            if (number instanceof BigInteger) {
                bigInt = (BigInteger) number;
            } else if (number instanceof BigDecimal) {
                bigInt = ((BigDecimal) number).toBigInteger();
            }
            // Effectively analogous to JDK 8's BigInteger.longValueExact()
            BigInteger longMin = BigInteger.valueOf(Long.MIN_VALUE);
            BigInteger longMax = BigInteger.valueOf(Long.MAX_VALUE);
            if (bigInt != null && (bigInt.compareTo(longMin) < 0 || bigInt.compareTo(longMax) > 0)) {
                throw new IllegalArgumentException("值" + number + "超出范围" + targetClass.getName());
            }
            return (T) Long.valueOf(number.longValue());
        } else if (BigInteger.class == targetClass) {
            if (number instanceof BigDecimal) {
                // do not lose precision - use BigDecimal's own conversion
                return (T) ((BigDecimal) number).toBigInteger();
            } else {
                // original value is not a Big* number - use standard long conversion
                return (T) BigInteger.valueOf(number.longValue());
            }
        } else if (Float.class == targetClass) {
            return (T) Float.valueOf(number.floatValue());
        } else if (Double.class == targetClass) {
            return (T) Double.valueOf(number.doubleValue());
        } else if (BigDecimal.class == targetClass) {
            // always use BigDecimal(String) here to avoid unpredictability of BigDecimal(double)
            // (see BigDecimal javadoc for details)
            return (T) new BigDecimal(number.toString());
        } else {
            throw new IllegalArgumentException("Could not convert number [" + number + "] of type ["
                    + number.getClass().getName() + "] to unsupported target class [" + targetClass.getName() + "]");
        }
    }

    @SuppressWarnings("unchecked")
    <T extends Number> T parseNumber(String text, Class<T> targetClass) {
        String trimmed = StringUtils.trim(text);

        if (Byte.class == targetClass) {
            return (T) (isHexNumber(trimmed) ? Byte.decode(trimmed) : Byte.valueOf(trimmed));
        } else if (Short.class == targetClass) {
            return (T) (isHexNumber(trimmed) ? Short.decode(trimmed) : Short.valueOf(trimmed));
        } else if (Integer.class == targetClass) {
            return (T) (isHexNumber(trimmed) ? Integer.decode(trimmed) : Integer.valueOf(trimmed));
        } else if (Long.class == targetClass) {
            return (T) (isHexNumber(trimmed) ? Long.decode(trimmed) : Long.valueOf(trimmed));
        } else if (BigInteger.class == targetClass) {
            return (T) (isHexNumber(trimmed) ? decodeBigInteger(trimmed) : new BigInteger(trimmed));
        } else if (Float.class == targetClass) {
            return (T) Float.valueOf(trimmed);
        } else if (Double.class == targetClass) {
            return (T) Double.valueOf(trimmed);
        } else if (BigDecimal.class == targetClass || Number.class == targetClass) {
            return (T) new BigDecimal(trimmed);
        } else {
            throw new IllegalArgumentException(
                    "Cannot convert String [" + text + "] to target class [" + targetClass.getName() + "]");
        }
    }

    /*
     * Determine whether the given {@code value} String indicates a hex number, i.e. needs to be passed into {@code
     * Integer.decode} instead of {@code Integer.valueOf}, etc.
     */
    private static boolean isHexNumber(String value) {
        int index = (value.startsWith("-") ? 1 : 0);
        return (value.startsWith("0x", index) || value.startsWith("0X", index) || value.startsWith("#", index));
    }

    /**
     * Decode a {@link java.math.BigInteger} from the supplied {@link String} value.
     * <p>
     * Supports decimal, hex, and octal notation.
     * 
     * @param value String
     * @see BigInteger#BigInteger(String, int)
     */
    private static BigInteger decodeBigInteger(String value) {
        int radix = 10;
        int index = 0;
        boolean negative = false;

        // Handle minus sign, if present.
        if (value.startsWith("-")) {
            negative = true;
            index++;
        }

        // Handle radix specifier, if present.
        if (value.startsWith("0x", index) || value.startsWith("0X", index)) {
            index += 2;
            radix = 16;
        } else if (value.startsWith("#", index)) {
            index++;
            radix = 16;
        } else if (value.startsWith("0", index) && value.length() > 1 + index) {
            index++;
            radix = 8;
        }

        BigInteger result = new BigInteger(value.substring(index), radix);
        return (negative ? result.negate() : result);
    }
}
