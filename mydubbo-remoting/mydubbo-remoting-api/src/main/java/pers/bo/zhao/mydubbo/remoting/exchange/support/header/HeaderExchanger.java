package pers.bo.zhao.mydubbo.remoting.exchange.support.header;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.remoting.RemotingException;
import pers.bo.zhao.mydubbo.remoting.Transporters;
import pers.bo.zhao.mydubbo.remoting.exchange.ExchangeHandler;
import pers.bo.zhao.mydubbo.remoting.exchange.ExchangeServer;
import pers.bo.zhao.mydubbo.remoting.exchange.Exchanger;
import pers.bo.zhao.mydubbo.remoting.transport.DecodeHandler;

public class HeaderExchanger implements Exchanger {


    @Override
    public ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {
        return new HeaderExchangeServer(Transporters.bind(url, new DecodeHandler(new HeaderExchangeHandler(handler))));
    }
}
