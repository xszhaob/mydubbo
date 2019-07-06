package pers.bo.zhao.mydubbo.remoting.transport;

import pers.bo.zhao.mydubbo.remoting.ChannelHandler;

/**
 * @author Bo.Zhao
 * @since 19/7/6
 */
public interface ChannelHandlerDelegate extends ChannelHandler {

    ChannelHandler getHandler();
}
