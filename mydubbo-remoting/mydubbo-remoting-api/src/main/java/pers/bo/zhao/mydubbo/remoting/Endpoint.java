package pers.bo.zhao.mydubbo.remoting;

import pers.bo.zhao.mydubbo.common.URL;

import java.net.InetSocketAddress;

/**
 * @author Bo.Zhao
 * @since 19/3/11
 */
public interface Endpoint {

    URL getUrl();

    ChannelHandler getChannelHander();

    InetSocketAddress getLocalAddress();

    void send(Object message) throws RemotingException;

    void send(Object message, boolean sent) throws RemotingException;

    void close();

    void clost(int timeout);

    void startClose();

    boolean isClosed();
}
