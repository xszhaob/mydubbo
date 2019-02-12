package pers.bo.zhao.mydubbo.common.extension.adaptive;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.extension.Adaptive;
import pers.bo.zhao.mydubbo.common.extension.SPI;

/**
 * @author Bo.Zhao
 * @since 19/2/11
 */
@SPI
public interface HasAdaptiveExt {

    @Adaptive
    String echo(URL url, String s);
}
