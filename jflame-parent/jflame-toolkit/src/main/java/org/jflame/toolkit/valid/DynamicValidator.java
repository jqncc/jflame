package org.jflame.toolkit.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.jflame.toolkit.valid.DynamicValid.ValidRule;

/**
 * 使用指定的内置验证规则验证
 * 
 * @see DynamicValid
 * @author yucan.zhang
 */
public class DynamicValidator implements ConstraintValidator<DynamicValid,Object> {

    private ValidRule[] rules;

    @Override
    public void initialize(DynamicValid constraintAnnotation) {
        rules = constraintAnnotation.rules();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        boolean flag = false;
        for (ValidRule rule : rules) {
            flag = false;
            switch (rule) {
                case mobile:
                    flag = ValidatorHelper.isMobileOrTel((String) value, 0);
                    break;
                case tel:
                    flag = ValidatorHelper.isMobileOrTel((String) value, 1);
                    break;
                case mobileOrTel:
                    flag = ValidatorHelper.isMobileOrTel((String) value, 2);
                    break;
                case idcard:
                    flag = ValidatorHelper.isIDCard((String) value);
                    break;
                case letter:
                    flag = ValidatorHelper.isLetterOrNumOrUnderline((String) value);
                    break;
                case letterNumOrline:
                    flag = ValidatorHelper.isLetter((String) value);
                    break;
                case ip:
                    flag = ValidatorHelper.isIPAddress((String) value);
                    break;
                case safeChar:
                    flag = ValidatorHelper.safeChar((String) value);
                    break;
                default:
                    break;
            }
            if (!flag) {
                break;
            }
        }
        return flag;
    }

}
