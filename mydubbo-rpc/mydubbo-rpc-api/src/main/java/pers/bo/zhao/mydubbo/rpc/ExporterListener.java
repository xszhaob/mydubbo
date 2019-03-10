package pers.bo.zhao.mydubbo.rpc;

import pers.bo.zhao.mydubbo.common.extension.SPI;

@SPI
public interface ExporterListener {

    void exported(Exporter<?> exporter) throws RpcException;

    void unexported(Exporter<?> exporter);
}
