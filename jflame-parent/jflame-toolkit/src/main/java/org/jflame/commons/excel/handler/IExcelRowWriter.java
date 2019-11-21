package org.jflame.commons.excel.handler;

import org.apache.poi.ss.usermodel.Row;

public interface IExcelRowWriter<T> {

    /**
     * 对象数据对填充excel行.
     * 
     * @param rowData 对象数据
     * @param excelRow excel行
     * @see org.apache.poi.ss.usermodel.Row
     */
    public void fillRow(T rowData, Row excelRow);
}
