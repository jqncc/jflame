package org.jflame.toolkit.excel.validator;

/**
 * excel单元格验证规则接口
 * 
 * @author yucan.zhang
 */
public interface IExcelValidator<T> {

    /**
     * 验证单个对象
     * 
     * @param entity 待验证对象
     * @param excelRowIndex excel行索引,即待验证对象在excel表里对应的行数
     * @throws ExcelValidationException 验证异常时抛出
     */
    void valid(T entity, Integer excelRowIndex) throws ExcelValidationException;

    /**
     * 验证集合里的所有对象
     * 
     * @param rowEntityMap 行索引与对象的map
     * @throws ExcelValidationException 验证异常时抛出
     */
    // void validList(Map<Integer,T> rowEntityMap) throws ExcelValidationException;

}
