package org.jflame.commons.convert;

import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

import org.jflame.commons.util.DateHelper;
import org.jflame.commons.util.StringHelper;

/**
 * 时间格式化
 * 
 * @author yucan.zhang
 */
public class DateToStringConverter extends ObjectToStringConverter<Date> {

    private String datePattern = null;

    public DateToStringConverter() {
    }

    public DateToStringConverter(String datePattern) {
        if (StringHelper.isNotEmpty(datePattern)) {
            this.datePattern = datePattern;
        }
    }

    @Override
    public String convert(Date source) {
        if (source instanceof java.sql.Date) {
            return DateFormatUtils.format(source, datePattern != null ? datePattern : DateHelper.YYYY_MM_DD);
        } else if (source instanceof java.sql.Time) {
            return DateFormatUtils.format(source, datePattern != null ? datePattern : DateHelper.HH_mm_ss);
        } else {
            return DateFormatUtils.format(source, datePattern != null ? datePattern : DateHelper.YYYY_MM_DD_HH_mm_ss);
        }
    }
}
