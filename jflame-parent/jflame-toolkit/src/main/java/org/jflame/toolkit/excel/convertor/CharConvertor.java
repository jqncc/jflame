package org.jflame.toolkit.excel.convertor;

import org.jflame.toolkit.exception.ConvertException;

public class CharConvertor implements ICellValueConvertor<Character> {

    @Override
    public Character convertFromExcel(Object cellValue, String pattern) throws ConvertException {
        if (cellValue != null) {
            return cellValue.toString().charAt(0);
        }
        return null;
    }

    @Override
    public String convertToExcel(Character value, String pattern) throws ConvertException {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    @Override
    public String getConvertorName() {
        return CellConvertorEnum.CHAR.toString();
    }

}
