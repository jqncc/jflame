package org.jflame.toolkit.valid;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 电话或手机号校验注解
 * 
 * @author yucan.zhang
 */
@Constraint(validatedBy = { PhoneValidator.class })
@Documented
@Target({ ElementType.ANNOTATION_TYPE,ElementType.METHOD,ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Phone {

    /**
     * 错误描述
     * 
     * @return
     */
    String message() default "{valid.telOrphone}";

    /**
     * 测试手机号
     * 
     * @return
     */
    boolean testMoble() default true;

    /**
     * 测试电话号
     * 
     * @return
     */
    boolean testTel() default true;

    /**
     * 校验分组
     * 
     * @return
     */
    Class<?>[] groups() default {};

    /**
     * @return
     */
    Class<? extends Payload>[] payload() default {};
}
