package org.jflame.toolkit.excel.convertor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.exception.ConvertException;

public class BoolConvertor implements ICellValueConvertor<Boolean> {

    private final String[] trueValues = { "是","true","1","y","yes" };
    private final String[] falseValues = { "否","false","0","n","no" };

    @Override
    public Boolean convertFromExcel(Object cellValue, String fmt) throws ConvertException {
        if (cellValue instanceof Boolean) {
            return (Boolean) cellValue;
        }
        if (cellValue == null) {
            return false;
        }
        String text = StringUtils.trim(String.valueOf(cellValue));
        if (ArrayUtils.contains(trueValues, text)) {
            return true;
        }
        return false;
    }

    @Override
    public String convertToExcel(Boolean value, String fmt) throws ConvertException {
        if (Boolean.TRUE.equals(value)) {
            return trueValues[0];
        } else if (Boolean.FALSE.equals(value)) {
            return falseValues[0];
        }
        return "";
    }

    @Override
    public String getConvertorName() {
        return CellConvertorEnum.BOOL.toString();
    }

}
