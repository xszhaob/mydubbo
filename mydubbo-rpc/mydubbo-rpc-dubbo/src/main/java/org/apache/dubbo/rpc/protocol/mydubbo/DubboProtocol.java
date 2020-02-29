package org.apache.dubbo.rpc.protocol.mydubbo;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.extension.ExtensionLoader;
import pers.bo.zhao.mydubbo.remoting.Transporter;
import pers.bo.zhao.mydubbo.remoting.exchange.ExchangeServer;
import pers.bo.zhao.mydubbo.rpc.Exporter;
import pers.bo.zhao.mydubbo.rpc.Invoker;
import pers.bo.zhao.mydubbo.rpc.Protocol;
import pers.bo.zhao.mydubbo.rpc.RpcException;
import pers.bo.zhao.mydubbo.rpc.protocol.AbstractProtocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DubboProtocol extends AbstractProtocol {
    public static final String NAME = "dubbo";

    public static final int DEFAULT_PORT = 20880;

    /**
     * <host:port,Exchanger>
     */
    private final Map<String, ExchangeServer> serverMap = new ConcurrentHashMap<>();
    private static DubboProtocol INSTANCE;


    @Override
    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        URL url = invoker.getUrl();

        String key = serviceKey(url);
        DubboExporter<T> exporter = new DubboExporter<>(invoker, key, exporterMap);
        exporterMap.put(key, exporter);

        // 打开服务
        openServer(url);
        optimizeSerialization(url);
        return exporter;
    }

    private void openServer(URL url) {
        boolean isServer = url.getParameter(Constants.IS_SERVER_KEY, true);
        if (isServer) {
            String address = url.getAddress();

            ExchangeServer server = serverMap.get(address);
            if (server == null) {
                synchronized (this) {
                    if (server == null) {
                        exporterMap.put(address, createServer(url));
                    }
                }
            } else {
                server.reset(url);
            }
        }


    }

    private Exporter<?> createServer(URL url) {
        // 服务关闭时发送只读事件
        url = url.addParameterIfAbsent(Constants.CHANNEL_READONLYEVENT_SENT_KEY, Boolean.TRUE.toString());
        // enable heartbeat by default
        // 默认发送心跳 60秒/次
        url = url.addParameterIfAbsent(Constants.HEARTBEAT_KEY, String.valueOf(Constants.DEFAULT_HEARTBEAT));
        // 默认netty
        String str = url.getParameter(Constants.SERVER_KEY, Constants.DEFAULT_REMOTING_SERVER);

        if (str != null && str.length() > 0 && !ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(str))
            throw new RpcException("Unsupported server type: " + str + ", url: " + url);

        // 编解码器，默认是dubbo协议
        url = url.addParameter(Constants.CODEC_KEY, DubboCodec.NAME);
        return null;

    }

    private void optimizeSerialization(URL url) {

    }

    @Override
    public <T> Invoker<T> refer(Class<?> type, URL url) throws RpcException {
        return null;
    }

    public static DubboProtocol getDubboProtocol() {
        if (INSTANCE == null) {
            ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(DubboProtocol.NAME); // load
        }
        return INSTANCE;
    }
}
