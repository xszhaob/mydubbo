package pers.bo.zhao.mydubbo.rpc.cluster;

import pers.bo.zhao.mydubbo.common.extension.SPI;
import pers.bo.zhao.mydubbo.rpc.cluster.loadbalance.RandomLoadBalance;

@SPI(RandomLoadBalance.NAME)
public interface LoadBalance {

}
