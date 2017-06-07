package org.jflame.db.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface TableGenerator {

    /**
     * 序列名称
     * 
     * @return
     */
    public String seqName() default "";

    /**
     * 每次增加的值，缺少50
     * 
     * @return
     */
    public int increment() default 50;

    /**
     * 初始值
     * 
     * @return
     */
    public int initialValue() default 1;

    public String tableName() default "sequence";

    public String pkColumnName() default "seq_name";

    public String valueColumnName() default "seq_value";
}
