package org.jflame.toolkit.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;

/**
 * 数字类型工具类
 * 
 * @author yucan.zhang
 */
public final class NumberHelper {

    /**
     * 字符串转int
     * 
     * @param text 待转换字符串
     * @return Integer
     */
    public static Integer parseInt(String text) {
        return parseNumber(text, Integer.class);
    }

    /**
     * 字符串转long
     * 
     * @param text 待转换字符串
     * @return Long
     */
    public static Long parseLong(String text) {
        return parseNumber(text, Long.class);
    }

    /**
     * 字符串转Double
     * 
     * @param text 待转换字符串
     * @return Double
     */
    public static Double parseDouble(String text) {
        return parseNumber(text, Double.class);
    }

    /**
     * 字符串转Float
     * 
     * @param text 待转换字符串
     * @return Float
     */
    public static Float parseFloat(String text) {
        return parseNumber(text, Float.class);
    }

    /**
     * 字符串转Short
     * 
     * @param text 待转换字符串
     * @return Short
     */
    public static Short parseShort(String text) {
        return parseNumber(text, Short.class);
    }

    /**
     * 转换字符串到相应的数字类型
     * 
     * @param text 待转换字符串
     * @param targetClass 数字类型
     * @return 相应的数字类型
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> T parseNumber(String text, Class<T> targetClass) {
        if (text == null) {
            return null;
        }
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

    /**
     * 转为Number类型数据到具体的子类
     * 
     * @param number
     * @param targetClass
     * @return
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> T convertNumberToSubclass(Number number, Class<T> targetClass)
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

    /**
     * 检测给定字符串是否是个16进制字符
     * 
     * @param value
     */
    public static boolean isHexNumber(String value) {
        int index = (value.startsWith("-") ? 1 : 0);
        return (value.startsWith("0x", index) || value.startsWith("0X", index) || value.startsWith("#", index));
    }

    /**
     * 转换字符串为BigInteger
     * <p>
     * 
     * @param value String
     * @see BigInteger#BigInteger(String, int)
     */
    public static BigInteger decodeBigInteger(String value) {
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
