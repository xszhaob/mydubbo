package pers.bo.zhao.mydubbo.remoting.exchange;

import pers.bo.zhao.mydubbo.remoting.ChannelHandler;
import pers.bo.zhao.mydubbo.remoting.RemotingException;

import java.util.concurrent.CompletableFuture;

public interface ExchangeHandler extends ChannelHandler {

    CompletableFuture<Object> reply(ExchangeChannel channel, Object request) throws RemotingException;
}
