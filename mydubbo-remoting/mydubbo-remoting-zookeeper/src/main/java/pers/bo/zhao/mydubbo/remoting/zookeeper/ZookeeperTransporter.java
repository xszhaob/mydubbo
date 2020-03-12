package pers.bo.zhao.mydubbo.remoting.zookeeper;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.extension.Adaptive;
import pers.bo.zhao.mydubbo.common.extension.SPI;

/**
 * @author Bo.Zhao
 * @since 19/4/10
 */
@SPI("curator")
public interface ZookeeperTransporter {
    @Adaptive({Constants.CLIENT_KEY, Constants.TRANSPORTER_KEY})
    ZookeeperClient connect(URL url);
}
