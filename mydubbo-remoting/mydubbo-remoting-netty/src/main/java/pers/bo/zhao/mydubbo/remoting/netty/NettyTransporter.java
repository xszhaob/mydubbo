package pers.bo.zhao.mydubbo.remoting.netty;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.remoting.*;


public class NettyTransporter implements Transporter {

    @Override
    public Server bind(URL url, ChannelHandler handler) throws RemotingException {
        return new NettyServer(url, handler);
    }

    @Override
    public Client connect(URL url, ChannelHandler handler) throws RemotingException {
        return null;
    }
}
