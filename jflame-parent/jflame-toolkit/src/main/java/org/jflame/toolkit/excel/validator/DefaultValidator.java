package org.jflame.toolkit.excel.validator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * 默认excel单元格数据验证器.
 * <br>使用Bean Validation规范验证
 * 
 * @author zyc
 */
public class DefaultValidator implements IValidator {
    protected ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    protected Validator validator = factory.getValidator();
    protected Map<Integer,String> errMap = new HashMap<Integer,String>();// 存放行号和对应错误信息

    @Override
    public <T> boolean valid(T entity, Integer excelRowIndex) {
        Set<ConstraintViolation<T>> errors = validator.validate(entity);
        if (!errors.isEmpty()) {
            buildErrMsg(excelRowIndex, errors);
        }
        return true;
    }

    @Override
    public <T> boolean validList(Map<Integer,T> rowEntityMap) {
        boolean result = true;
        Set<ConstraintViolation<T>> errors;
        for (Entry<Integer,T> kv : rowEntityMap.entrySet()) {
            errors = validator.validate(kv.getValue());
            if (!errors.isEmpty()) {
                result = false;
                buildErrMsg(kv.getKey(), errors);
            }
        }
        return result;
    }

    /**
     * 获取验证后的错误信息.
     */
    @Override
    public Map<Integer,String> getErrors() {
        return errMap;
    }

    private <T> void buildErrMsg(Integer excelRowIndex, Set<ConstraintViolation<T>> errors) {
        String err = "第" + (excelRowIndex + 1) + "行,";
        Iterator<ConstraintViolation<T>> it = errors.iterator();
        while (it.hasNext()) {
            ConstraintViolation<T> cv = (ConstraintViolation<T>) it.next();
            err = err + cv.getMessage() + ';';
        }
        errMap.put(excelRowIndex, err);
    }
}
