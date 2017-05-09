package org.jflame.toolkit.excel.convertor;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.util.DateHelper;

/**
 * excel日期转换器. <br>
 * 默认支持解析时间格式有:<br>
 * yyyy-MM-dd<br>
 * yyyy-MM-dd HH:mm:ss<br>
 * yyyy年MM月dd日 <br>
 * yyyyMMddHHmmss<br>
 * yyyy/MM/dd<br>
 * 默认转换到excel的字符格式yyyy-MM-dd HH:mm:ss
 * 
 * @author zyc
 */
public class DateConvertor implements ICellValueConvertor<Date> {

    private Class<? extends Date> dateClass;

    public DateConvertor() {
    }

    public DateConvertor(Class<? extends Date> dateClass) {
        this.dateClass = dateClass;
    }

    @Override
    public Date convertFromExcel(Object cellValue,final String fmt) throws ConvertException {
        Date resultVal = null;
        if (cellValue instanceof Date) {
            resultVal = (Date) cellValue;
        } else {
            if (cellValue != null) {
                String text = StringUtils.trim(String.valueOf(cellValue));
                if (StringUtils.isNotEmpty(fmt)) {
                    resultVal = DateHelper.parseDate(text, fmt);
                } else {
                    resultVal = DateHelper.parseDate(String.valueOf(cellValue), DateHelper.YYYY_MM_DD_HH_mm_ss,
                            DateHelper.YYYY_MM_DD, DateHelper.CN_YYYY_MM_DD, DateHelper.yyyyMMddHHmmss,
                            "yyyy/MM/dd");
                }
            }
        }
        if (resultVal != null && dateClass != null) {
            if (java.sql.Date.class == dateClass) {
                resultVal = new java.sql.Date(resultVal.getTime());
            }
            if (java.sql.Timestamp.class == dateClass) {
                resultVal = new java.sql.Timestamp(resultVal.getTime());
            }
        }
        return resultVal;
    }

    @Override
    public String convertToExcel(Date value, final String fmt) throws ConvertException {
        SimpleDateFormat datFormator = new SimpleDateFormat();

        if (StringUtils.isNotEmpty(fmt)) {
            try {
                datFormator.applyPattern(fmt);
            } catch (IllegalArgumentException e) {
                throw new ConvertException(e);
            }
        } else {
            datFormator.applyPattern(DateHelper.YYYY_MM_DD_HH_mm_ss);
        }
        return datFormator.format(value);
    }

    public String getConvertorName() {
        return CellConvertorEnum.date.name();
    }

}
