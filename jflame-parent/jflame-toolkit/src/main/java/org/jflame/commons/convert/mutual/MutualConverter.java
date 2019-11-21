package org.jflame.commons.convert.mutual;

import org.jflame.commons.convert.Converter;

/**
 * 两种类型相互转换接口
 * 
 * @author yucan.zhang
 * @param <S> 类型1
 * @param <T> 类型2
 */
public interface MutualConverter<S,T> extends Converter<S,T> {

    T convert(S source);

    S inverseConvert(T source);
}
