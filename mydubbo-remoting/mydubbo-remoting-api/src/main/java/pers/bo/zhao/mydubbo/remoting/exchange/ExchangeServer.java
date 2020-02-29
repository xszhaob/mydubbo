package pers.bo.zhao.mydubbo.remoting.exchange;

import pers.bo.zhao.mydubbo.remoting.Server;

import java.net.InetSocketAddress;
import java.util.Collection;

public interface ExchangeServer extends Server {

    Collection<ExchangeChannel> getExchangeChannels();


    ExchangeChannel getExchangeChannel(InetSocketAddress remoteAddress);
}
