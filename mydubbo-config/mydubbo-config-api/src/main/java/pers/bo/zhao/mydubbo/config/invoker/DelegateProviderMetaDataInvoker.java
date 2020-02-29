package pers.bo.zhao.mydubbo.config.invoker;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.config.ServiceConfig;
import pers.bo.zhao.mydubbo.rpc.Invocation;
import pers.bo.zhao.mydubbo.rpc.Invoker;
import pers.bo.zhao.mydubbo.rpc.Result;
import pers.bo.zhao.mydubbo.rpc.RpcException;

public class DelegateProviderMetaDataInvoker<T> implements Invoker<T> {

    protected final Invoker<T> invoker;

    private ServiceConfig metaData;


    public DelegateProviderMetaDataInvoker(Invoker<T> invoker, ServiceConfig metaData) {
        this.invoker = invoker;
        this.metaData = metaData;
    }

    @Override
    public Class<?> getInterface() {
        return invoker.getInterface();
    }

    @Override
    public URL getUrl() {
        return invoker.getUrl();
    }

    @Override
    public boolean isAvailable() {
        return invoker.isAvailable();
    }

    @Override
    public void destroy() {
        invoker.destroy();
    }

    public ServiceConfig getMetaData() {
        return metaData;
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        return null;
    }
}
