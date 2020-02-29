package pers.bo.zhao.mydubbo.rpc.proxy;

import pers.bo.zhao.mydubbo.rpc.Invoker;
import pers.bo.zhao.mydubbo.rpc.ProxyFactory;
import pers.bo.zhao.mydubbo.rpc.RpcException;

public abstract class AbstractProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(Invoker<T> invoker) throws RpcException {
        return getProxy(invoker, false);
    }

    @Override
    public <T> T getProxy(Invoker<T> invoker, boolean generic) throws RpcException {
        throw new UnsupportedOperationException();
    }

}
