package org.jflame.toolkit.convert.mutual;

import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

import org.jflame.toolkit.convert.StringToDateConverter;
import org.jflame.toolkit.util.DateHelper;

/**
 * 时间类型java.util.Date及子类与字符串互转.
 * <p>
 * 1.该类继承{@link StringToDateConverter},所以字符串转为时间的行为与父类保持一至.<br>
 * 2.在未指定格式情况下,时间转为字符串默认格式如下:<br>
 * java.sql.Date -&gt; yyyy-MM-dd<br>
 * java.sql.Time -&gt; HH:mm:ss<br>
 * java.util.Date/java.sql.Timestamp -&gt; yyyy-MM-dd HH:mm:ss
 * 
 * @author yucan.zhang
 * @param <T> T extends Date
 */
public class StringAndDateConverter<T extends Date> extends StringToDateConverter<T> implements MutualConverter<String,T> {

    public StringAndDateConverter() {
        super();
    }

    public StringAndDateConverter(String pattern) {
        super(pattern);
    }

    public StringAndDateConverter(Class<T> targetType) {
        super(targetType);
    }

    public StringAndDateConverter(Class<T> targetType, String pattern) {
        super(targetType, pattern);
    }

    @Override
    public String inverseConvert(T source) {
        if (source instanceof java.sql.Date) {
            return DateFormatUtils.format(source,
                    getPattern().isPresent() ? getPattern().get() : DateHelper.YYYY_MM_DD);
        } else if (source instanceof java.sql.Time) {
            return DateFormatUtils.format(source, getPattern().isPresent() ? getPattern().get() : DateHelper.HH_mm_ss);
        } else {
            return DateFormatUtils.format(source,
                    getPattern().isPresent() ? getPattern().get() : DateHelper.YYYY_MM_DD_HH_mm_ss);
        }
    }

}
