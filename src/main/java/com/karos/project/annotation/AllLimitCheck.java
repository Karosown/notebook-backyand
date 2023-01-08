package com.karos.project.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllLimitCheck {
    /**
     * 任意内容
     *
     * @return
     */
    String[] anyText() default "";

    /**
     * 指定内容
     *
     * @return
     */
    String mustText() default "";
}
