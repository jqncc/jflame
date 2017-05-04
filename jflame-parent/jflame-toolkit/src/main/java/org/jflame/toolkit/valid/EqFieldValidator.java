package org.jflame.toolkit.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.jflame.toolkit.reflect.ReflectionHelper;

/**
 * 属性相等比较
 * 
 * @see EqField
 * @author yucan.zhang
 */
public class EqFieldValidator implements ConstraintValidator<EqField,Object> {

    private String field;
    private String eqField;

    @Override
    public void initialize(EqField constraintAnnotation) {
        field = constraintAnnotation.field();
        eqField = constraintAnnotation.eqField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        boolean flag = false;
        try {
            Object fieldValue = ReflectionHelper.getFieldValue(value, field);
            Object eqFieldValue = ReflectionHelper.getFieldValue(value, eqField);
            if (fieldValue == null && eqFieldValue == null) {
                flag = true;
            } else if (fieldValue != null && eqFieldValue != null) {
                flag = fieldValue.equals(eqFieldValue);
            }
            if (!flag) {
                String messageTemplate = context.getDefaultConstraintMessageTemplate();
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(messageTemplate).addNode(eqField).addConstraintViolation();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return flag;
    }

}
