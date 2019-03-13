package pers.bo.zhao.mydubbo.common.status;

import pers.bo.zhao.mydubbo.common.extension.SPI;

/**
 * @author Bo.Zhao
 * @since 19/3/13
 */
@SPI
public interface StatusChecker {

    Status check();
}
