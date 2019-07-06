package pers.bo.zhao.mydubbo.remoting;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * 远程服务端。（API/SPI，原型，线程安全）
 *
 * @author Bo.Zhao
 * @since 19/7/6
 */
public interface Server extends Endpoint, Channel, Resetable, IdleSensible {

    boolean isBound();


    Collection<Channel> getChannels();


    Channel getChannel(InetSocketAddress remoteAddress);

}
