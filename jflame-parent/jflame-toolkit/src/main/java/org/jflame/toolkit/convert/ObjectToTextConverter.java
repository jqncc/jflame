package org.jflame.toolkit.convert;

public class ObjectToTextConverter<S> implements Converter<S,String> {

    @Override
    public String convert(S source) {
        if (source != null) {
            return String.valueOf(source);
        }
        return null;
    }

}
