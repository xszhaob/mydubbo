package pers.bo.zhao.mydubbo.remoting.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.utils.CollectionUtils;
import pers.bo.zhao.mydubbo.common.utils.NetUtils;
import pers.bo.zhao.mydubbo.remoting.Channel;
import pers.bo.zhao.mydubbo.remoting.ChannelHandler;
import pers.bo.zhao.mydubbo.remoting.RemotingException;
import pers.bo.zhao.mydubbo.remoting.Server;
import pers.bo.zhao.mydubbo.remoting.transport.AbstractServer;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NettyServer extends AbstractServer implements Server {

    private Map<String, Channel> channels;

    private io.netty.channel.Channel channel;

    private ServerBootstrap bootstrap;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;


    public NettyServer(URL url, ChannelHandler handler) throws RemotingException {
        super(url, handler);
    }


    @Override
    protected void doOpen() throws Throwable {
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1,
                new DefaultThreadFactory("NettyServerBoss", true));
        workerGroup = new NioEventLoopGroup(
                getUrl().getParameter(Constants.IO_THREADS_KEY, Constants.DEFAULT_IO_THREADS),
                new DefaultThreadFactory("NettyServerWorker", true));

        final NettyServerHandler nettyServerHandler = new NettyServerHandler(getUrl(), this);
        channels = nettyServerHandler.getChannels();

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        NettyCodecAdapter adapter = new NettyCodecAdapter(getCodec(), getUrl(), NettyServer.this);
                        ch.pipeline().addLast("decoder", adapter.getDecoder());
                        ch.pipeline().addLast("encoder", adapter.getEncoder());
                        ch.pipeline().addLast("handler", nettyServerHandler);
                    }
                });

        ChannelFuture future = bootstrap.bind(getBindAddress());
        future.syncUninterruptibly();
        channel = future.channel();
    }

    @Override
    protected void doClose() {
        // server端的channel
        if (channel != null) {
            channel.close();
        }
        // 连接到该server上的客户端channel
        Collection<Channel> channels = getChannels();
        if (CollectionUtils.isNotEmpty(channels)) {
            for (Channel channel : channels) {
                channel.close();
            }
        }

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        channels.clear();
    }

    @Override
    public Collection<Channel> getChannels() {
        Set<Channel> chs = new HashSet<>(channels.size());

        for (Channel value : this.channels.values()) {
            if (value.isConnected()) {
                chs.add(value);
            } else {
                channels.remove(NetUtils.toAddressString(value.getRemoteAddress()));
            }
        }
        return chs;
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        return channels.get(NetUtils.toAddressString(remoteAddress));
    }

    @Override
    public boolean isBound() {
        return channel.isActive();
    }
}
