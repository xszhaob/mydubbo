package pers.bo.zhao.mydubbo.rpc.cluster;

import pers.bo.zhao.mydubbo.common.extension.SPI;
import pers.bo.zhao.mydubbo.rpc.cluster.support.FailoverCluster;

/**
 * @author Bo.Zhao
 * @since 19/3/8
 */
@SPI(FailoverCluster.NAME)
public interface Cluster {
}
