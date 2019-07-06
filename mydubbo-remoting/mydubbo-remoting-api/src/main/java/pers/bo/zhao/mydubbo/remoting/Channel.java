package pers.bo.zhao.mydubbo.remoting;

import java.net.InetSocketAddress;

public interface Channel extends Endpoint {
    /**
     * 获取远程地址
     *
     * @return
     */
    InetSocketAddress getRemoteAddress();


    boolean isConnected();

    boolean hasAttribute(String key);

    Object getAttribute(String key);

    void setAttribute(String key, Object value);

    void removeAttribute(String key);
}
