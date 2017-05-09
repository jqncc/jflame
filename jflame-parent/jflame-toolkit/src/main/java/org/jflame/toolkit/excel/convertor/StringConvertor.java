package org.jflame.toolkit.excel.convertor;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.util.DateHelper;
import org.jflame.toolkit.util.StringHelper;

public class StringConvertor implements ICellValueConvertor<String> {

    @Override
    public String convertFromExcel(Object cellValue, String pattern) throws ConvertException {
        String value = null;
        if (cellValue instanceof String) {
            value = (String) cellValue;
        } else if (cellValue instanceof Number) {
            NumberFormat formator = NumberFormat.getInstance();
            formator.setGroupingUsed(false);// 不分组，避免科学计数等
            value = formator.format(cellValue);
        } else if (cellValue instanceof Date) {
            value = DateHelper.format((Date) cellValue, DateHelper.YYYY_MM_DD_HH_mm_ss);
        } else {
            value = String.valueOf(cellValue);
        }
        return value;
    }

    @Override
    public String convertToExcel(final String value, final String pattern) throws ConvertException {
        if (StringHelper.isNotEmpty(pattern)) {
            return MessageFormat.format(pattern, value);
        }
        return value;
    }

    @Override
    public String getConvertorName() {
        return CellConvertorEnum.string.name();
    }
}
