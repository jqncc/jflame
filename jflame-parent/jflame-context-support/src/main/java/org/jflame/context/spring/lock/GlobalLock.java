package org.jflame.context.spring.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface GlobalLock {

    /**
     * 锁名,可以是SPEL表达式
     * 
     * @return
     */
    String lockName();

    /**
     * 过期时间,单位:秒s
     * 
     * @return
     */
    int lockTime();

    /**
     * 获取锁的待时间,单位:毫秒ms
     * 
     * @return
     */
    int waitTime() default 0;
}
