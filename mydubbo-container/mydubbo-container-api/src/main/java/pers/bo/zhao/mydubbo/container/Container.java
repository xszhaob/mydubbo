package pers.bo.zhao.mydubbo.container;

import pers.bo.zhao.mydubbo.common.extension.SPI;

/**
 * @author Bo.Zhao
 * @since 19/3/1
 */
@SPI("spring")
public interface Container {


    void start();


    void stop();
}
