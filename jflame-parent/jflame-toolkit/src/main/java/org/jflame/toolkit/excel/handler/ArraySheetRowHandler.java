package org.jflame.toolkit.excel.handler;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.jflame.toolkit.excel.convertor.ExcelConvertorSupport;

public class ArraySheetRowHandler implements ISheetRowHandler<Object[]> {

    @Override
    public void fillRow(Object[] rowData, Row excelSheetRow) {
        if (ArrayUtils.isEmpty(rowData)) {
            return;
        }
        Cell cell;
        for (int i = 0; i < rowData.length; i++) {
            cell = excelSheetRow.createCell(i);
            setValueToCell(ExcelConvertorSupport.convertToCellValue(null, rowData[i]), cell);
        }
    }

    @Override
    public Object[] extractRow(Row excelSheetRow) {
        int firstIndex = excelSheetRow.getFirstCellNum();
        int lastIndex = excelSheetRow.getLastCellNum();
        Object[] newObjs = new Object[lastIndex - firstIndex];
        for (int i = firstIndex, j = 0; i < lastIndex; i++, j++) {
            newObjs[j] = ExcelConvertorSupport.getCellValue(excelSheetRow.getCell(i));
        }
        return newObjs;
    }

    void setValueToCell(Object propertyValue, Cell cell) {
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue(ExcelConvertorSupport.convertToCellValue(null, propertyValue));
    }
}
