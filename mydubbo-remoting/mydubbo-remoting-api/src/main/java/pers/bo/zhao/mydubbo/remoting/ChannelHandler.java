package pers.bo.zhao.mydubbo.remoting;

/**
 * @author Bo.Zhao
 * @since 19/3/11
 */
public interface ChannelHandler {

    /**
     * 连接
     *
     * @param channel
     * @throws RemotingException
     */
    void connected(Channel channel) throws RemotingException;

    /**
     * 断开连接
     *
     * @param channel
     * @throws RemotingException
     */
    void disconnected(Channel channel) throws RemotingException;

    void sent(Channel channel, Object message) throws RemotingException;

    /**
     * @param channel
     * @param message
     * @throws RemotingException
     */
    void received(Channel channel, Object message) throws RemotingException;

    /**
     * on exception caught.
     *
     * @param channel   channel.
     * @param exception exception.
     */
    void caught(Channel channel, Throwable exception) throws RemotingException;

}
