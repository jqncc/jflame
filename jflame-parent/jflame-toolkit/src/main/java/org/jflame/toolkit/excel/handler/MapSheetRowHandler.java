package org.jflame.toolkit.excel.handler;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.jflame.toolkit.excel.ExcelAccessException;
import org.jflame.toolkit.excel.convertor.ExcelConvertorSupport;

/**
 * excel单行数据与LinkedHashMap转换处理器. <br>
 * 注：使用有序的LinkedHashMap
 * 
 * @author yucan.zhang
 */
public class MapSheetRowHandler implements ISheetRowHandler<LinkedHashMap<String,Object>> {

    private String[] excludeKeys;// 需要排除的键,即不写入excel
    private String[] importKeys;

    public MapSheetRowHandler() {
    }

    /**
     * 构造
     * 
     * @param keys 导入时的map keys,顺序跟excel列一致
     */
    public MapSheetRowHandler(String[] keys) {
        if (keys == null || keys.length == 0) {
            throw new ExcelAccessException("未指定要导入的map keys");
        }
        importKeys = keys;
    }

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
        if (importKeys == null || importKeys.length == 0) {
            throw new ExcelAccessException("未指定要导入的map keys");
        }
        LinkedHashMap<String,Object> dataMap = new LinkedHashMap<>();
        for (int i = 0; i < importKeys.length; i++) {
            dataMap.put(importKeys[i],
                    ExcelConvertorSupport.getCellValue(excelSheetRow.getCell(i, Row.RETURN_NULL_AND_BLANK)));
        }
        return dataMap;
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
        cell.setCellValue(ExcelConvertorSupport.convertToCellValue(null, propertyValue));
    }

    public void setExcludeKeys(String[] excludeKeys) {
        this.excludeKeys = excludeKeys;
    }

}
