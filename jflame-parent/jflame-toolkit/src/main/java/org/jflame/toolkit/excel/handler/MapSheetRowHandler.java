package org.jflame.toolkit.excel.handler;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.jflame.toolkit.excel.convertor.ExcelConvertorSupport;

/**
 * excel单行数据与LinkedHashMap转换处理器. <br>
 * 注：使用有序的LinkedHashMap
 * 
 * @author yucan.zhang
 */
public class MapSheetRowHandler implements ISheetRowHandler<LinkedHashMap<String,Object>> {

    private String[] excludeKeys;// 需要排除的键,即不写入excel

    @Override
    public void fillRow(LinkedHashMap<String,Object> rowData, Row excelSheetRow) {
        int cellIndex = 0;
        Cell cell;
        Iterator<Entry<String,Object>> it = rowData.entrySet().iterator();
        Entry<String,Object> entry;
        while (it.hasNext()) {
            entry = it.next();
            if (!isExcude(entry.getKey())) {
                cell = excelSheetRow.createCell(cellIndex++);
                setValueToCell(entry.getValue(), cell);
            }
        }
    }

    @Override
    public LinkedHashMap<String,Object> extractRow(Row excelSheetRow) {
        return null;
    }

    boolean isExcude(String key) {
        if (ArrayUtils.isEmpty(excludeKeys)) {
            return false;
        } else {
            return ArrayUtils.contains(excludeKeys, key);
        }
    }

    void setValueToCell(Object propertyValue, Cell cell) {
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue(ExcelConvertorSupport.convertToCellValue(null, propertyValue,null));
    }

    public void setExcludeKeys(String[] excludeKeys) {
        this.excludeKeys = excludeKeys;
    }

}
