package org.jflame.toolkit.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 使用指定的内置验证规则验证
 * 
 * @see DynamicValid
 * @author yucan.zhang
 */
public class DynamicValidator implements ConstraintValidator<DynamicValid,Object> {

    /**
     * 内置验证规则
     * 
     * @author yucan.zhang
     */
    public enum ValidRule {
        /**
         * 手机
         */
        mobile,
        /**
         * 电话号
         */
        tel,
        /**
         * 电话或手机号
         */
        mobileOrTel,
        /**
         * 身份证验证
         */
        idcard,
        /**
         * 字母
         */
        letter,
        /**
         * 字母,数字或下划线
         */
        letterNumOrline,
        /**
         * ip地址
         */
        ip,
        /**
         * 不包含特殊字符*%\=<>`';?&!
         */
        safeChar
    }

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
