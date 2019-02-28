package pers.bo.zhao.mydubbo.config.api;

import pers.bo.zhao.mydubbo.common.extension.SPI;

/**
 * @author Bo.Zhao
 * @since 19/2/28
 */
@SPI
public interface Greeting {
    String hello();
}
