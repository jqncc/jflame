package org.jflame.toolkit.valid;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.jflame.toolkit.valid.DynamicValidator.ValidRule;

/**
 * 动态验证注解,使用指定的内置验证规则验证
 * @author yucan.zhang
 *
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
     * 指定验证规则
     * 
     * @see org.jflame.toolkit.valid.DynamicValidator.ValidRule
     * @return
     */
    ValidRule rule();

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
