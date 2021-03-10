package org.jflame.commons.excel.handler;

import org.apache.poi.ss.usermodel.Row;

import org.jflame.commons.excel.ExcelConvertUtils;

public class ArrayRowReader implements IExcelRowReader<Object[]> {

    private int firstIndex;
    private int lastIndex;
    private Object[] newObjs;

    @Override
    public Object[] extractRow(Row excelSheetRow) {
        firstIndex = excelSheetRow.getFirstCellNum();
        lastIndex = excelSheetRow.getLastCellNum();
        newObjs = new Object[lastIndex - firstIndex];
        for (int i = firstIndex, j = 0; i < lastIndex; i++, j++) {
            newObjs[j] = ExcelConvertUtils
                    .getCellValue(excelSheetRow.getCell(i, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK));
        }
        return newObjs;
    }

}
