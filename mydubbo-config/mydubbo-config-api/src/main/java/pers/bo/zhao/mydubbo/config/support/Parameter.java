package pers.bo.zhao.mydubbo.config.support;

import java.lang.annotation.*;

/**
 * @author Bo.Zhao
 * @since 19/2/12
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Parameter {

    String key() default "";

    boolean required() default false;

    boolean excluded() default false;

    boolean escaped() default false;

    boolean attribute() default false;

    boolean append() default false;
}
