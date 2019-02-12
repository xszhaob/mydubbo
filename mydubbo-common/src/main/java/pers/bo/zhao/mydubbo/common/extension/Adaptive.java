package pers.bo.zhao.mydubbo.common.extension;

import java.lang.annotation.*;

/**
 * @author Bo.Zhao
 * @since 19/1/27
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Adaptive {

    String[] value() default {};
}
