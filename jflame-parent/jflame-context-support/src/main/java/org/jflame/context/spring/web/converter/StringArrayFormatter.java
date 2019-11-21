package org.jflame.context.spring.web.converter;

import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.Formatter;

import org.jflame.commons.util.StringHelper;

/**
 * @author yucan.zhang
 */
public class StringArrayFormatter implements Formatter<String[]> {

    @Override
    public String print(String[] objs, Locale locale) {
        return StringHelper.join(objs);
    }

    @Override
    public String[] parse(String text, Locale locale) throws ParseException {
        if (text == null) {
            return null;
        }
        return StringHelper.split(StringUtils.deleteWhitespace(text));
    }

}
