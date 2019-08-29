package org.jflame.toolkit.excel.handler;

import org.apache.poi.ss.usermodel.Row;

public interface IExcelRowReader<T> {

    /**
     * excel行转对象数据类型
     * 
     * @param excelSheetRow excel行
     * @see org.apache.poi.ss.usermodel.Row
     * @return 对象数据
     */
    public T extractRow(Row excelSheetRow);
}
