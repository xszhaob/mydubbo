package pers.bo.zhao.mydubbo.rpc.cluster.loadbalance;

import pers.bo.zhao.mydubbo.rpc.Invocation;
import pers.bo.zhao.mydubbo.rpc.Invoker;
import pers.bo.zhao.mydubbo.rpc.RpcInvocation;
import pers.bo.zhao.mydubbo.rpc.RpcStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bo.Zhao
 * @since 19/6/29
 */
public class LoadBalanceBaseTest {

    Invocation invocation;
    List<Invoker<LoadBalanceBaseTest>> invokers = new ArrayList<Invoker<LoadBalanceBaseTest>>();
    Invoker<LoadBalanceBaseTest> invoker1;
    Invoker<LoadBalanceBaseTest> invoker2;
    Invoker<LoadBalanceBaseTest> invoker3;
    Invoker<LoadBalanceBaseTest> invoker4;
    Invoker<LoadBalanceBaseTest> invoker5;

    RpcStatus weightTestRpcStatus1;
    RpcStatus weightTestRpcStatus2;
    RpcStatus weightTestRpcStatus3;

    RpcInvocation weightTestInvocation;
}
