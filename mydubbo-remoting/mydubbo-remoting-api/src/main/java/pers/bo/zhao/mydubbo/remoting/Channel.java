package pers.bo.zhao.mydubbo.remoting;

import java.net.InetSocketAddress;

public interface Channel extends Endpoint {

    InetSocketAddress getRemoteAddress();


    boolean isConnected();

    boolean hasAttribute(String key);

    Object getAttribute(String key);

    void setAttribute(String key, Object value);

    void removeAttribute(String key);
}
