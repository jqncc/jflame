package org.jflame.commons.convert;

/**
 * 类型转换接口
 * 
 * @author yucan.zhang
 * @param <S> 要转换的类型
 * @param <T> 目标类型
 */
public interface Converter<S,T> {

    /**
     * 转换
     * 
     * @param source
     * @return
     */
    T convert(S source);
}
