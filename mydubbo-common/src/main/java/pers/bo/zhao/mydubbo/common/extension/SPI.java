package pers.bo.zhao.mydubbo.common.extension;

import java.lang.annotation.*;

/**
 * @author Bo.Zhao
 * @since 19/1/26
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    /**
     * 扩展点默认实现的别名
     */
    String value() default "";
}
