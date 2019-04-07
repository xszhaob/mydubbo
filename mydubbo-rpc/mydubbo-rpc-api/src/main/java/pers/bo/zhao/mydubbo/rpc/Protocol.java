package pers.bo.zhao.mydubbo.rpc;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.extension.Adaptive;
import pers.bo.zhao.mydubbo.common.extension.SPI;

@SPI
public interface Protocol {

    int getDefaultPort();

    @Adaptive
    <T> Exporter<T> export() throws RpcException;

    @Adaptive
    <T> Invoker<T> refer(Class<?> type, URL url) throws RpcException;

    void destroy();

}
