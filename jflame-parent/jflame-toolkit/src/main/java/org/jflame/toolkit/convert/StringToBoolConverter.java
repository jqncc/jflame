package org.jflame.toolkit.convert;

import java.util.HashSet;
import java.util.Set;

import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.util.StringHelper;

/**
 * string转boolean
 * 
 * @author yucan.zhang
 */
public class StringToBoolConverter implements Converter<String,Boolean> {

    private static final Set<String> trueValues = new HashSet<String>(6);
    private static final Set<String> falseValues = new HashSet<String>(6);

    static {
        trueValues.add("true");
        trueValues.add("1");
        trueValues.add("on");
        trueValues.add("yes");
        trueValues.add("y");
        trueValues.add("是");

        falseValues.add("false");
        falseValues.add("0");
        falseValues.add("off");
        falseValues.add("no");
        falseValues.add("n");
        falseValues.add("否");
    }

    @Override
    public Boolean convert(String source) {
        String value = source.trim();
        if (StringHelper.isEmpty(source)) {
            return null;
        }
        value = value.toLowerCase();
        if (trueValues.contains(value)) {
            return Boolean.TRUE;
        } else if (falseValues.contains(value)) {
            return Boolean.FALSE;
        } else {
            throw new ConvertException("不能转为 boolean '" + source + "'");
        }
    }

}
