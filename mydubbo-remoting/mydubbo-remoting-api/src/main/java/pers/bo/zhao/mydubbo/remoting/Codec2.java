package pers.bo.zhao.mydubbo.remoting;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.extension.Adaptive;
import pers.bo.zhao.mydubbo.common.extension.SPI;
import pers.bo.zhao.mydubbo.remoting.buffer.ChannelBuffer;

import java.io.IOException;

/**
 * dubbo数据传输的编码解码接口
 *
 * @author Bo.Zhao
 * @since 19/7/6
 */
@SPI
public interface Codec2 {

    /**
     * 编码，在客户端发送消息时，
     * 需要将请求对象按照一定的格式（二进制流）将对象编码成二进制流，
     * 以便消息接收端能正确从二进流中按照格式解码出一个完整的请求信息
     */
    @Adaptive({Constants.CODEC_KEY})
    void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException;

    /**
     * 解码，在消息接受端，按照协议的规范，从二进制流中解码出一个一个的请求信息，以便处理。
     */
    @Adaptive({Constants.CODEC_KEY})
    Object decode(Channel channel, ChannelBuffer buffer) throws IOException;


    enum DecodeResult {
        /**
         * 在解码过程中如果收到的字节流不是一个完整包时，结束此次读事件处理，等待更多数据到达
         */
        NEED_MORE_INPUT,
        /**
         * 忽略掉一部分输入数据
         */
        SKIP_SOME_INPUT
    }
}
