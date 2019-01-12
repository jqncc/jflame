package org.jflame.toolkit.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.reflect.ReflectionHelper;

/**
 * 属性相等比较
 * 
 * @see EqField
 * @author yucan.zhang
 */
public class AnyFieldNotNullValidator implements ConstraintValidator<AnyFieldNotNull,Object> {

    private String[] fields;
    private boolean emptyIsNull;

    @Override
    public void initialize(AnyFieldNotNull constraintAnnotation) {
        fields = constraintAnnotation.fields();
        emptyIsNull = constraintAnnotation.emptyIsNull();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            for (String field : fields) {
                Object fieldValue = ReflectionHelper.getFieldValue(value, field);
                if (emptyIsNull) {
                    if (fieldValue != null && !StringUtils.EMPTY.equals(fieldValue)) {
                        return true;
                    }
                } else {
                    if (fieldValue != null) {
                        return true;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        String messageTemplate = context.getDefaultConstraintMessageTemplate();
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addConstraintViolation();

        return false;
    }

}
