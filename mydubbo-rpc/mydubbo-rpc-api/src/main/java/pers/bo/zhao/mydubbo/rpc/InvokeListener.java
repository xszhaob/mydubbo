package pers.bo.zhao.mydubbo.rpc;

import pers.bo.zhao.mydubbo.common.extension.SPI;

@SPI
public interface InvokeListener {


    void refered(Invoker<?> invoker) throws RpcException;

    void destoryed(Invoker<?> invoker);
}
