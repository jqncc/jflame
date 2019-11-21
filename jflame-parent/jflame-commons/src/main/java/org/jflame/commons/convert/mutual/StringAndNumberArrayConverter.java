package org.jflame.commons.convert.mutual;

import java.lang.reflect.Array;

import org.jflame.commons.util.NumberHelper;
import org.jflame.commons.util.StringHelper;

/**
 * 字符串和数字数组互转.数组以英文逗号组合为字符串. 如"1,2,3" =&gt; {1,2,3}
 * 
 * @author yucan.zhang
 * @param <T>
 */
public class StringAndNumberArrayConverter<T extends Number> implements MutualConverter<String,Number[]> {

    Class<? extends Number> clazz;

    public StringAndNumberArrayConverter(Class<? extends Number> targetType) {
        clazz = targetType;
    }

    @Override
    public Number[] convert(String source) {
        if (NumberHelper.isNumberType(clazz)) {
            return convert(source);
        } else {
            return convertToNumberArray(source);
        }
    }

    @Override
    public String inverseConvert(Number[] source) {
        return StringHelper.join(source);
    }

    private Number[] convertToNumberArray(String text) {
        if (StringHelper.isNotEmpty(text)) {
            String[] strArr = StringHelper.split(text);
            Number[] numArr = (Number[]) Array.newInstance(clazz, strArr.length);
            for (int i = 0; i < strArr.length; i++) {
                numArr[i] = NumberHelper.parseNumber(strArr[i], clazz);
            }
            return numArr;
        }
        return null;
    }
}
