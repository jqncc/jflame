package org.jflame.commons.valid.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.jflame.commons.valid.AnyFieldNotNullValidator;

/**
 * 验证注解.用于标记类中多个属性必须有一个不为空
 * 
 * @author yucan.zhang
 */
@Constraint(validatedBy = { AnyFieldNotNullValidator.class })
@Documented
@Target({ ElementType.ANNOTATION_TYPE,ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AnyFieldNotNull {

    /**
     * 错误描述
     * 
     * @return
     */
    String message();

    /**
     * 要判断的属性
     * 
     * @return
     */
    String[] fields();

    /**
     * 空字符串是否作为null,默认false
     * 
     * @return
     */
    boolean emptyIsNull() default false;

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
}
