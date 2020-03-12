package pers.bo.zhao.mydubbo.remoting.zookeeper.curator;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.remoting.zookeeper.ZookeeperClient;
import pers.bo.zhao.mydubbo.remoting.zookeeper.ZookeeperTransporter;

public class CuratorZookeeperTransporter implements ZookeeperTransporter {

    @Override
    public ZookeeperClient connect(URL url) {
        return new CuratorZookeeperClient(url);
    }

}
