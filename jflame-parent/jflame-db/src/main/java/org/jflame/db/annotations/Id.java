package org.jflame.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jflame.db.id.IdType;

/**
 * 表主键
 * 
 * @author yucan.zhang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Id {

    /**
     * 对应列名
     * 
     * @return
     */
    String name() default "";

    /**
     * @return
     */
    IdType idType() default IdType.ASSIGN;
}
