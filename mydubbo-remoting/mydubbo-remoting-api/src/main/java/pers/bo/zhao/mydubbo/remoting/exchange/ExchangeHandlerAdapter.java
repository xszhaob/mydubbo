package pers.bo.zhao.mydubbo.remoting.exchange;

import pers.bo.zhao.mydubbo.remoting.RemotingException;
import pers.bo.zhao.mydubbo.remoting.transport.ChannelHandlerAdapter;

import java.util.concurrent.CompletableFuture;

public class ExchangeHandlerAdapter extends ChannelHandlerAdapter implements ExchangeHandler {

    @Override
    public CompletableFuture<Object> reply(ExchangeChannel channel, Object msg) throws RemotingException {
        return null;
    }
}
