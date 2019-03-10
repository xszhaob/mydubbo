package pers.bo.zhao.mydubbo.rpc;


public interface Exporter<T> {

    Invoker<T> getInvoker();

    void unexport();
}
