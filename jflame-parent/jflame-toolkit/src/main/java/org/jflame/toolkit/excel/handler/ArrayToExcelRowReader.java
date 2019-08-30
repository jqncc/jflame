package org.jflame.toolkit.excel.handler;

import org.jflame.toolkit.excel.ExcelUtils;

import org.apache.poi.ss.usermodel.Row;

public class ArrayToExcelRowReader implements IExcelRowReader<Object[]> {

    private int firstIndex;
    private int lastIndex;
    private Object[] newObjs;

    @Override
    public Object[] extractRow(Row excelSheetRow) {
        firstIndex = excelSheetRow.getFirstCellNum();
        lastIndex = excelSheetRow.getLastCellNum();
        newObjs = new Object[lastIndex - firstIndex];
        for (int i = firstIndex, j = 0; i < lastIndex; i++, j++) {
            newObjs[j] = ExcelUtils
                    .getCellValue(excelSheetRow.getCell(i, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK));
        }
        return newObjs;
    }

}
