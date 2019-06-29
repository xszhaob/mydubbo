package pers.bo.zhao.mydubbo.rpc.cluster.loadbalance;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.utils.CollectionUtils;
import pers.bo.zhao.mydubbo.rpc.Invocation;
import pers.bo.zhao.mydubbo.rpc.Invoker;
import pers.bo.zhao.mydubbo.rpc.RpcException;
import pers.bo.zhao.mydubbo.rpc.cluster.LoadBalance;

import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        if (CollectionUtils.isEmpty(invokers)) {
            return null;
        }

        if (invokers.size() == 1) {
            return invokers.get(0);
        }

        return doSelect(invokers, url, invocation);
    }


    protected abstract <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException;


    protected int getWeight(Invoker<?> invoker, Invocation invocation) {
        int weight = invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT);
        if (weight > 0) {
            long timestamp = invoker.getUrl().getParameter(Constants.REMOTE_TIMESTAMP_KEY, 0L);
            if (timestamp > 0L) {
                int uptime = (int) (System.currentTimeMillis() - timestamp);
                int warmup = invoker.getUrl().getParameter(Constants.WARMUP_KEY, Constants.DEFAULT_WARMUP);
                if (uptime > 0 && uptime < warmup) {
                    weight = calculateWarmupWeight(uptime, warmup, weight);
                }
            }
        }
        return weight >= 0 ? weight : 0;
    }


    static int calculateWarmupWeight(int uptime, int warmup, int weight) {
        int ww = (int) ((float) uptime / ((float) warmup / (float) weight));
        return ww < 1 ? 1 : (ww > weight ? weight : ww);
    }
}
