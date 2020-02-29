package pers.bo.zhao.mydubbo.remoting;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.extension.Adaptive;
import pers.bo.zhao.mydubbo.common.extension.SPI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Codec. (SPI, Singleton, ThreadSafe)
 */
@Deprecated
@SPI
public interface Codec {

    /**
     * Need more input poison.
     *
     * @see #decode(Channel, InputStream)
     */
    Object NEED_MORE_INPUT = new Object();

    /**
     * Encode message.
     *
     * @param channel channel.
     * @param output  output stream.
     * @param message message.
     */
    @Adaptive({Constants.CODEC_KEY})
    void encode(Channel channel, OutputStream output, Object message) throws IOException;

    /**
     * Decode message.
     *
     * @param channel channel.
     * @param input   input stream.
     * @return message or <code>NEED_MORE_INPUT</code> poison.
     * @see #NEED_MORE_INPUT
     */
    @Adaptive({Constants.CODEC_KEY})
    Object decode(Channel channel, InputStream input) throws IOException;

}