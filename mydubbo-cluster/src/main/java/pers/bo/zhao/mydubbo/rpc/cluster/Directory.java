package pers.bo.zhao.mydubbo.rpc.cluster;

import pers.bo.zhao.mydubbo.common.Node;
import pers.bo.zhao.mydubbo.rpc.Invocation;
import pers.bo.zhao.mydubbo.rpc.Invoker;
import pers.bo.zhao.mydubbo.rpc.RpcException;

import java.util.List;

/**
 * @author Bo.Zhao
 * @since 19/3/8
 */
public interface Directory<T> extends Node {

    Class<T> getInterface();

    List<Invoker<T>> list(Invocation invocation) throws RpcException;
}
