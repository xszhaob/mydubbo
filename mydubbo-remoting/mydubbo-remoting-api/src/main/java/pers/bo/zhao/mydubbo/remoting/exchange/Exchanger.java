package pers.bo.zhao.mydubbo.remoting.exchange;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.extension.Adaptive;
import pers.bo.zhao.mydubbo.common.extension.SPI;
import pers.bo.zhao.mydubbo.remoting.RemotingException;

/**
 * @author Bo.Zhao
 * @since 19/3/13
 */
@SPI
public interface Exchanger {

    @Adaptive(Constants.EXCHANGER_KEY)
    ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException;
}
