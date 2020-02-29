package pers.bo.zhao.mydubbo.rpc.proxy.javassist;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.bytecode.Wrapper;
import pers.bo.zhao.mydubbo.rpc.Invoker;
import pers.bo.zhao.mydubbo.rpc.RpcException;
import pers.bo.zhao.mydubbo.rpc.proxy.AbstractProxyFactory;
import pers.bo.zhao.mydubbo.rpc.proxy.AbstractProxyInvoker;

public class JavassistProxyFactory extends AbstractProxyFactory {

    @Override
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) throws RpcException {
        Wrapper wrapper = Wrapper.getWrapper(proxy.getClass().getName().indexOf('$') < 0 ? proxy.getClass() : type);
        return new AbstractProxyInvoker<T>(proxy, type, url) {
            @Override
            protected Object doInvoke(T proxy, String methodName, Class<?>[] parameterTypes, Object[] arguments) throws Throwable {
                return wrapper.invokeMethod(proxy, methodName, parameterTypes, arguments);
            }
        };
    }
}
