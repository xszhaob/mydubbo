package pers.bo.zhao.mydubbo.rpc.cluster.loadbalance;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.rpc.Invocation;
import pers.bo.zhao.mydubbo.rpc.Invoker;
import pers.bo.zhao.mydubbo.rpc.RpcException;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "random";


    @Override
    public <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        int length = invokers.size();
        // 每个Invoker的权重是否相同
        boolean sameWeight = true;
        // 每个Invoker的权重
        int[] weights = new int[length];
        // 计算第一个Invoker的权重
        int firstWeight = getWeight(invokers.get(0), invocation);
        // 总权重
        int totalWeight = firstWeight;
        // 为第一个Invoker赋权重值
        weights[0] = firstWeight;
        for (int i = 1; i < invokers.size(); i++) {
            Invoker<T> invoker = invokers.get(i);

            int weight = getWeight(invoker, invocation);
            weights[i] = weight;
            totalWeight += weight;

            if (sameWeight && firstWeight != weight) {
                sameWeight = false;
            }
        }

        if (!sameWeight && totalWeight > 0) {
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);

            for (int i = 0; i < length; i++) {
                offset -= weights[i];
                if (offset < 0) {
                    return invokers.get(i);
                }
            }
        }

        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }
}
