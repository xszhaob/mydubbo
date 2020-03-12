package pers.bo.zhao.mydubbo.remoting.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.remoting.Codec2;

import javax.print.DocFlavor;
import java.util.List;

public class NettyCodecAdapter {

    private final ChannelHandler encoder = new InternalEncoder();

    private final ChannelHandler decoder = new InternalDecoder();

    private final Codec2 codec2;

    private final URL url;

    private final pers.bo.zhao.mydubbo.remoting.ChannelHandler channelHandler;

    public NettyCodecAdapter(Codec2 codec2, URL url, pers.bo.zhao.mydubbo.remoting.ChannelHandler channelHandler) {
        this.codec2 = codec2;
        this.url = url;
        this.channelHandler = channelHandler;
    }


    public ChannelHandler getEncoder() {
        return encoder;
    }

    public ChannelHandler getDecoder() {
        return decoder;
    }

    private static class InternalEncoder extends MessageToByteEncoder {
        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

        }
    }


    private static class InternalDecoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        }
    }
}
