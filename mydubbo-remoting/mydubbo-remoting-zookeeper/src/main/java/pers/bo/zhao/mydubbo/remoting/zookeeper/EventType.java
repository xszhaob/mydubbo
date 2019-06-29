package pers.bo.zhao.mydubbo.remoting.zookeeper;

import org.apache.zookeeper.Watcher;

/**
 * @author Bo.Zhao
 * @since 19/4/10
 */
public enum EventType {

    NONE(-1),
    NODECREATED(1),
    NODEDELETED(2),
    NODEDATACHANGED(3),
    NODECHILDRENCHANGED(4),
    CONNECTION_SUSPENDED(11),
    CONNECTION_RECONNECTED(12),
    CONNECTION_LOST(12),
    INITIALIZED(10);

    private final int intValue;

    EventType(int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static Watcher.Event.EventType fromInt(int intValue) {
        switch (intValue) {
            case -1:
                return Watcher.Event.EventType.None;
            case 1:
                return Watcher.Event.EventType.NodeCreated;
            case 2:
                return Watcher.Event.EventType.NodeDeleted;
            case 3:
                return Watcher.Event.EventType.NodeDataChanged;
            case 4:
                return Watcher.Event.EventType.NodeChildrenChanged;

            default:
                throw new RuntimeException("Invalid integer value for conversion to EventType");
        }
    }
}
