package pers.bo.zhao.mydubbo.remoting.zookeeper;

import pers.bo.zhao.mydubbo.common.URL;

import java.util.List;

/**
 * @author Bo.Zhao
 * @since 19/4/10
 */
public interface ZookeeperClient {
    void create(String path, boolean ephemeral);

    void delete(String path);

    List<String> getChildren(String path);

    List<String> addChildListener(String path, ChildListener listener);

    void removeChildListener(String path, ChildListener listener);

    void addStateListener(StateListener listener);

    void removeStateListener(StateListener listener);

    boolean isConnected();

    void close();

    URL getUrl();
}
