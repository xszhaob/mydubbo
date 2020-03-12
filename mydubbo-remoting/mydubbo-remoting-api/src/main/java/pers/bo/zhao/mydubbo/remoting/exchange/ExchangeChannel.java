package pers.bo.zhao.mydubbo.remoting.exchange;

import pers.bo.zhao.mydubbo.remoting.Channel;
import pers.bo.zhao.mydubbo.remoting.RemotingException;

public interface ExchangeChannel extends Channel {
    /**
     * send request.
     *
     * @param request
     * @return response future
     * @throws RemotingException
     */
    ResponseFuture request(Object request) throws RemotingException;

    /**
     * send request.
     *
     * @param request
     * @param timeout
     * @return response future
     * @throws RemotingException
     */
    ResponseFuture request(Object request, int timeout) throws RemotingException;

    /**
     * get message handler.
     *
     * @return message handler
     */
    ExchangeHandler getExchangeHandler();

    /**
     * graceful close.
     *
     * @param timeout
     */
    @Override
    void close(int timeout);
}
