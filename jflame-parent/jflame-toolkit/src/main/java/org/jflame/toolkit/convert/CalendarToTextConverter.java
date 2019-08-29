package org.jflame.toolkit.convert;

import java.util.Calendar;

import org.jflame.toolkit.util.DateHelper;
import org.jflame.toolkit.util.StringHelper;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 时间格式化
 * 
 * @author yucan.zhang
 */
public class CalendarToTextConverter extends ObjectToTextConverter<Calendar> {

    private String datePattern = null;

    public CalendarToTextConverter() {
    }

    public CalendarToTextConverter(String datePattern) {
        if (StringHelper.isNotEmpty(datePattern)) {
            this.datePattern = datePattern;
        }
    }

    @Override
    public String convert(Calendar source) {
        return DateFormatUtils.format(source, datePattern != null ? datePattern : DateHelper.YYYY_MM_DD_HH_mm_ss);
    }
}
