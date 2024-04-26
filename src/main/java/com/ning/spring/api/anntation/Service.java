package com.ning.spring.api.anntation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {
    /**
    * Bean 装载识别ID
     */
    String value() default "";
}
