package org.jflame.toolkit.convert;

import org.jflame.toolkit.util.NumberHelper;

/**
 * string转为Number及子类.
 * <p>
 * 支持的类型包括:Byte, Short, Integer, Float, Double, Long, BigInteger, BigDecimal.
 * 
 * @author yucan.zhang
 */
public final class TextToNumberConverterFactory implements ConverterFactory<String,Number> {

    @Override
    public <T extends Number> Converter<String,T> getConverter(Class<T> targetType) {
        return new StringToNumber<T>(targetType);
    }

    private static final class StringToNumber<T extends Number> implements Converter<String,T> {

        private final Class<T> targetType;

        public StringToNumber(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(String source) {
            if (source.isEmpty()) {
                return null;
            }
            return NumberHelper.parseNumber(source, this.targetType);
        }
    }
}
