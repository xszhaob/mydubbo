package pers.bo.zhao.mydubbo.remoting;

import pers.bo.zhao.mydubbo.common.URL;

import java.net.InetSocketAddress;

/**
 * 端点接口
 *
 * @author Bo.Zhao
 * @since 19/3/11
 */
public interface Endpoint {
    /**
     * 获取端点的URL
     */
    URL getUrl();

    /**
     * 获取管道处理器
     *
     * @return
     */
    ChannelHandler getChannelHandler();

    /**
     * 获取本地地址
     *
     * @return
     */
    InetSocketAddress getLocalAddress();

    /**
     * 发送消息
     *
     * @param message
     * @throws RemotingException
     */
    void send(Object message) throws RemotingException;

    /**
     * 发送消息
     *
     * @param message
     * @param sent    是否已经发送过消息
     * @throws RemotingException
     */
    void send(Object message, boolean sent) throws RemotingException;

    /**
     * 关闭通道
     */
    void close();

    /**
     * 优雅关闭
     *
     * @param timeout
     */
    void close(int timeout);

    void startClose();

    /**
     * todo
     * 端点是否已关闭？或是管道是否已关闭？
     *
     * @return
     */
    boolean isClosed();
}
