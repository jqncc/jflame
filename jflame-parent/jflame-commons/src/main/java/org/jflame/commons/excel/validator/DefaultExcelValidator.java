package org.jflame.commons.excel.validator;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * 默认excel单元格数据验证器. <br>
 * 使用Bean Validation规范验证
 * 
 * @author zyc
 */
public class DefaultExcelValidator<T> implements IExcelValidator<T> {

    protected ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    protected Validator validator = factory.getValidator();
    // protected Map<Integer,String> errMap = new HashMap<Integer,String>();// 存放行号和对应错误信息

    @Override
    public void valid(T entity, Integer excelRowIndex) throws ExcelValidationException {
        Set<ConstraintViolation<T>> errors = validator.validate(entity);
        if (!errors.isEmpty()) {
            throw new ExcelValidationException(excelRowIndex, errors);
        }
    }

    /*  @Override
    public void validList(Map<Integer,T> rowEntityMap) throws ExcelValidationException {
        Set<ConstraintViolation<T>> errors;
        Map<Integer,Set<? extends ConstraintViolation<?>>> errMap = new HashMap<>();
        for (Entry<Integer,T> kv : rowEntityMap.entrySet()) {
            errors = validator.validate(kv.getValue());
            if (!errors.isEmpty()) {
                errMap.put(kv.getKey(), errors);
            }
        }
        if (!errMap.isEmpty()) {
            throw new ExcelValidationException(errMap);
        }
    }*/

}
