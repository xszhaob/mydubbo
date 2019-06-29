package pers.bo.zhao.mydubbo.rpc.cluster;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.extension.Adaptive;
import pers.bo.zhao.mydubbo.common.extension.SPI;
import pers.bo.zhao.mydubbo.rpc.Invocation;
import pers.bo.zhao.mydubbo.rpc.Invoker;
import pers.bo.zhao.mydubbo.rpc.RpcException;
import pers.bo.zhao.mydubbo.rpc.cluster.loadbalance.RandomLoadBalance;

import java.util.List;

@SPI(RandomLoadBalance.NAME)
public interface LoadBalance {

    /**
     * select one invoker in list.
     *
     * @param invokers   invokers.
     * @param url        refer url
     * @param invocation invocation.
     * @return selected invoker.
     */
    @Adaptive("loadbalance")
    <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException;
}
