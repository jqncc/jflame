package org.jflame.toolkit.valid;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 动态验证注解,使用指定的内置验证规则验证
 * 
 * @author yucan.zhang
 */
@Constraint(validatedBy = { DynamicValidator.class })
@Documented
@Target({ ElementType.ANNOTATION_TYPE,ElementType.METHOD,ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicValid {

    /**
     * 错误描述
     * 
     * @return
     */
    String message();

    /**
     * 指定验证规则,多条规则是and关系
     * 
     * @see org.jflame.toolkit.valid.DynamicValidator.ValidRule
     * @return
     */
    ValidRule[] rules();

    /**
     * 校验分组
     * 
     * @return
     */
    Class<?>[] groups() default {};

    /**
     * 用于同一属性不同条件时的验证区分
     * 
     * @return
     */
    Class<? extends Payload>[] payload() default {};
    
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

}
