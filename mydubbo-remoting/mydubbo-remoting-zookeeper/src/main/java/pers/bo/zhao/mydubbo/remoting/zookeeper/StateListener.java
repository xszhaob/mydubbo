package pers.bo.zhao.mydubbo.remoting.zookeeper;

/**
 * @author Bo.Zhao
 * @since 19/4/10
 */
public interface StateListener {
    int DISCONNECTED = 0;

    int CONNECTED = 1;

    int RECONNECTED = 2;

    void stateChanged(int connected);
}
