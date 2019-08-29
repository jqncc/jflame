package org.jflame.toolkit.convert;

public interface ConverterFactory<S,R> {

    /**
     * 获取一个S到T的转换器,其中T可以是类泛型参数R的子类
     * 
     * @param <T> 目标类型
     * @param targetType 要转换的目标类型class
     * @return
     */
    <T extends R> Converter<S,T> getConverter(Class<T> targetType);
}
