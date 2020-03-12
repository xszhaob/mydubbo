package pers.bo.zhao.mydubbo.remoting.zookeeper;

import java.util.List;

/**
 * @author Bo.Zhao
 * @since 19/4/10
 */
public interface ChildListener {
    void childChanged(String path, List<String> children);
}
