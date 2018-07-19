package org.jflame.toolkit.excel.validator;

import java.util.Map;

/**
 * excel单元格验证规则接口
 * 
 * @author yucan.zhang
 */
public interface IValidator<T> {

    /**
     * 验证单个对象
     * 
     * @param entity 待验证对象
     * @param excelRowIndex excel行索引,即待验证对象在excel表里对应的行数
     * @return 验证通过返回true,否则false
     */
    boolean valid(T entity, Integer excelRowIndex);

    /**
     * 验证集合里的所有对象
     * 
     * @param rowEntityMap 行索引与对象的map
     * @return 只要有一个对象验证失败即返回false
     */
    boolean validList(Map<Integer,T> rowEntityMap);

    /**
     * 获取行索引和对应错误信息的map.
     * 
     * @return 验证结果信息
     */
    Map<Integer,String> getErrors();

}
