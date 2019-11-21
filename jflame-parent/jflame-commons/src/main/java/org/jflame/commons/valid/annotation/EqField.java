package org.jflame.commons.valid.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.jflame.commons.valid.EqFieldValidator;

/**
 * 两个属性相等校验注解，放类型上
 * 
 * @author yucan.zhang
 */
@Constraint(validatedBy = { EqFieldValidator.class })
@Documented
@Target({ ElementType.ANNOTATION_TYPE,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EqField {

    /**
     * 错误描述
     * 
     * @return
     */
    String message();

    /**
     * 校验分组
     * 
     * @return
     */
    Class<?>[] groups() default {};

    /**
     * 被比较属性名
     * 
     * @return
     */
    String field();

    /**
     * 待比较属性名
     * 
     * @return
     */
    String eqField();

    /**
     * 用于同一属性不同条件时的验证区分
     * 
     * @return
     */
    Class<? extends Payload>[] payload() default {};
}
