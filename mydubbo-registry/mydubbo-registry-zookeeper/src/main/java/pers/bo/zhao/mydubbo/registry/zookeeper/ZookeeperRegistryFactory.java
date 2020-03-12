package pers.bo.zhao.mydubbo.registry.zookeeper;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.registry.Registry;
import pers.bo.zhao.mydubbo.registry.support.AbstractRegistryFactory;
import pers.bo.zhao.mydubbo.remoting.zookeeper.ZookeeperTransporter;

/**
 * ZookeeperRegistryFactory.
 *
 */
public class ZookeeperRegistryFactory extends AbstractRegistryFactory {

    private ZookeeperTransporter zookeeperTransporter;

    public void setZookeeperTransporter(ZookeeperTransporter zookeeperTransporter) {
        this.zookeeperTransporter = zookeeperTransporter;
    }

    @Override
    public Registry createRegistry(URL url) {
        return new ZookeeperRegistry(url, zookeeperTransporter);
    }

}
