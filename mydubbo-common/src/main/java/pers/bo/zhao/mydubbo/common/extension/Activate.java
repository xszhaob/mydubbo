package pers.bo.zhao.mydubbo.common.extension;

import java.lang.annotation.*;

/**
 * @author Bo.Zhao
 * @since 19/1/28
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Activate {

    String[] group() default {};

    String[] value() default {};

    int order() default 0;
}
