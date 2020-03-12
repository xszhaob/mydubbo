package pers.bo.zhao.mydubbo.common.compiler;

import pers.bo.zhao.mydubbo.common.extension.SPI;

/**
 * @author Bo.Zhao
 * @since 19/1/26
 */
@SPI("javassist")
public interface Compiler {

    Class<?> compile(String code, ClassLoader classLoader);
}
