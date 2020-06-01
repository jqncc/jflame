package org.jflame.commons.excel.validator;

import java.util.Iterator;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.jflame.commons.excel.ExcelAccessException;
import org.jflame.commons.model.Chars;

/**
 * excel数据验证异常
 * 
 * @author yucan.zhang
 */
public class ExcelValidationException extends ExcelAccessException {

    private static final long serialVersionUID = -6715041074239673913L;

    public ExcelValidationException(String message) {
        super(message);
    }

    /**
     * 构造函数.
     * 
     * @param excelRowIndex 发生异常的excel行索引
     * @param constraintViolations 验证异常
     */
    public ExcelValidationException(int excelRowIndex, Set<? extends ConstraintViolation<?>> constraintViolations) {
        this(buildErrMsg(excelRowIndex, constraintViolations));
    }

    /**
     * 构造函数.
     * 
     * @param excelRowIndex 发生异常的excel行索引
     * @param constraintViolations 验证异常
     */
    /* public ExcelValidationException(Map<Integer,Set<? extends ConstraintViolation<?>>> rowIndexAndViolations) {
        this(buildErrMsgs(rowIndexAndViolations));
    }
    
    private static String buildErrMsgs(Map<Integer,Set<? extends ConstraintViolation<?>>> rowIndexAndViolations) {
        StringBuilder stringBuilder = new StringBuilder();
        rowIndexAndViolations.forEach((k, v) -> {
            stringBuilder.append(buildErrMsg(k, v))
                    .append(';');
        });
        return stringBuilder.toString();
    }*/

    private static String buildErrMsg(int excelRowIndex, Set<? extends ConstraintViolation<?>> errors) {
        String err = "第" + (excelRowIndex + 1) + "行 ";
        Iterator<? extends ConstraintViolation<?>> it = errors.iterator();
        while (it.hasNext()) {
            ConstraintViolation<?> cv = it.next();
            err = err + cv.getMessage() + Chars.COMMA;
        }
        return err;
    }
}
