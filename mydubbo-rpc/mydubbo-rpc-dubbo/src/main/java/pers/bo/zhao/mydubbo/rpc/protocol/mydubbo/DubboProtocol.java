package pers.bo.zhao.mydubbo.rpc.protocol.mydubbo;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.extension.ExtensionLoader;
import pers.bo.zhao.mydubbo.remoting.Channel;
import pers.bo.zhao.mydubbo.remoting.RemotingException;
import pers.bo.zhao.mydubbo.remoting.Transporter;
import pers.bo.zhao.mydubbo.remoting.exchange.*;
import pers.bo.zhao.mydubbo.rpc.*;
import pers.bo.zhao.mydubbo.rpc.protocol.AbstractProtocol;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DubboProtocol extends AbstractProtocol {
    public static final String NAME = "dubbo";

    public static final int DEFAULT_PORT = 20880;

    /**
     * <host:port,Exchanger>
     */
    private final Map<String, ExchangeServer> serverMap = new ConcurrentHashMap<>();
    private static DubboProtocol INSTANCE;


    private ExchangeHandler requestHandler = new ExchangeHandlerAdapter() {
        @Override
        public CompletableFuture<Object> reply(ExchangeChannel channel, Object message) throws RemotingException {
            // 只处理invocation的情况
            if (message instanceof Invocation) {
                Invocation inv = (Invocation) message;

                Invoker<?> invoker = getInvoker(channel, inv);

                RpcContext rpcContext = RpcContext.getContext();
                rpcContext.setRemoteAddress(channel.getRemoteAddress());
                Result result = invoker.invoke(inv);

                return CompletableFuture.completedFuture(result);
            }
            throw new RemotingException(channel, "Unsupported request: "
                    + (message == null ? null : (message.getClass().getName() + ": " + message))
                    + ", channel: consumer: " + channel.getRemoteAddress() + " --> provider: " + channel.getLocalAddress());
        }


        @Override
        public void received(Channel channel, Object message) throws RemotingException {
            if (message instanceof Invocation) {
                reply((ExchangeChannel) channel, message);
            } else {
                super.received(channel, message);
            }
        }

        @Override
        public void connected(Channel channel) throws RemotingException {
            invoke(channel, Constants.ON_CONNECT_KEY);
        }

        @Override
        public void disconnected(Channel channel) throws RemotingException {
            if (logger.isInfoEnabled()) {
                logger.info("disconnected from " + channel.getRemoteAddress() + ",url:" + channel.getUrl());
            }
            invoke(channel, Constants.ON_DISCONNECT_KEY);
        }

        private void invoke(Channel channel, String methodKey) {
            Invocation invocation = createInvocation(channel.getUrl(), methodKey);
            if (invocation != null) {
                try {
                    received(channel, invocation);
                } catch (Throwable t) {
                    logger.warn("Failed to invoke event method " + invocation.getMethodName() + "(), cause: " + t.getMessage(), t);
                }
            }
        }


        private Invocation createInvocation(URL url, String methodKey) {
            String method = url.getParameter(methodKey);
            if (method == null || method.length() == 0) {
                return null;
            }
            RpcInvocation invocation = new RpcInvocation(method, new Class<?>[0], new Object[0]);
            invocation.setAttachment(Constants.PATH_KEY, url.getPath());
            invocation.setAttachment(Constants.GROUP_KEY, url.getParameter(Constants.GROUP_KEY));
            invocation.setAttachment(Constants.INTERFACE_KEY, url.getParameter(Constants.INTERFACE_KEY));
            invocation.setAttachment(Constants.VERSION_KEY, url.getParameter(Constants.VERSION_KEY));
            if (url.getParameter(Constants.STUB_EVENT_KEY, false)) {
                invocation.setAttachment(Constants.STUB_EVENT_KEY, Boolean.TRUE.toString());
            }
            return invocation;
        }
    };


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
                        serverMap.put(address, createServer(url));
                    }
                }
            } else {
                server.reset(url);
            }
        }
    }

    private ExchangeServer createServer(URL url) {
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

        ExchangeServer server;

        try {
            server = Exchangers.bind(url, requestHandler);
        } catch (RemotingException e) {
            throw new RpcException("Fail to start server(url: " + url + ") " + e.getMessage(), e);
        }

        return server;
    }

    private void optimizeSerialization(URL url) {
        // no op
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        return null;
    }

    public static DubboProtocol getDubboProtocol() {
        if (INSTANCE == null) {
            ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(DubboProtocol.NAME); // load
        }
        return INSTANCE;
    }

    Invoker<?> getInvoker(Channel channel, Invocation inv) throws RemotingException {
        int port = channel.getLocalAddress().getPort();
        String path = inv.getAttachments().get(Constants.PATH_KEY);
        String version = inv.getAttachments().get(Constants.VERSION_KEY);
        String group = inv.getAttachments().get(Constants.GROUP_KEY);
        String serviceKey = serviceKey(port, path, version, group);

        Exporter<?> exporter = exporterMap.get(serviceKey);
        return exporter.getInvoker();
    }
}
