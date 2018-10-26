package org.jflame.toolkit.excel.convertor;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.util.DateHelper;

/**
 * excel日期Calendar转换器. <br>
 * 
 * @author zyc
 */
public class CalendarConvertor implements ICellValueConvertor<Calendar> {

    @Override
    public Calendar convertFromExcel(Object cellValue, final String fmt) throws ConvertException {
        if (cellValue instanceof Date) {
            return DateUtils.toCalendar((Date) cellValue);
        } else if (cellValue instanceof Calendar) {
            return (Calendar) cellValue;
        } else {
            Date resultVal = null;
            if (cellValue != null && !StringUtils.EMPTY.equals(cellValue)) {
                String text = StringUtils.trim(String.valueOf(cellValue));
                if (StringUtils.isNotEmpty(fmt)) {
                    resultVal = DateHelper.parseDate(text, fmt);
                } else {
                    resultVal = DateHelper.parseDate(text, DateHelper.YYYY_MM_DD_HH_mm_ss, DateHelper.YYYY_MM_DD,
                            DateHelper.CN_YYYY_MM_DD, DateHelper.yyyyMMddHHmmss, "yyyy/MM/dd");
                }
                return DateUtils.toCalendar(resultVal);
            }
        }
        return null;
    }

    @Override
    public String convertToExcel(Calendar value, final String fmt) throws ConvertException {
        return DateFormatUtils.format(value, StringUtils.isNotEmpty(fmt) ? fmt : DateHelper.YYYY_MM_DD_HH_mm_ss);
    }

    public String getConvertorName() {
        return CellConvertorEnum.calendar.name();
    }

}
