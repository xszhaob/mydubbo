package org.apache.dubbo.rpc.protocol.mydubbo;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.Version;
import pers.bo.zhao.mydubbo.common.io.Bytes;
import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;
import pers.bo.zhao.mydubbo.common.utils.StringUtils;
import pers.bo.zhao.mydubbo.remoting.Channel;
import pers.bo.zhao.mydubbo.remoting.Codec2;
import pers.bo.zhao.mydubbo.remoting.exchange.Request;
import pers.bo.zhao.mydubbo.remoting.exchange.Response;
import pers.bo.zhao.mydubbo.remoting.exchange.codec.ExchangerCodec;
import pers.bo.zhao.mydubbo.remoting.transport.CodecSupport;
import pers.bo.zhao.mydubbo.rpc.Invocation;
import pers.bo.zhao.mydubbo.serialize.ObjectInput;
import pers.bo.zhao.mydubbo.serialize.Serialization;

import java.io.IOException;
import java.io.InputStream;

public class DubboCodec extends ExchangerCodec implements Codec2 {
    private static final Logger logger = LoggerFactory.getLogger(ExchangerCodec.class);

    public static final String NAME = "dubbo";
    public static final String DUBBO_VERSION = Version.getProtocolVersion();
    public static final byte RESPONSE_WITH_EXCEPTION = 0;
    public static final byte RESPONSE_VALUE = 1;
    public static final byte RESPONSE_NULL_VALUE = 2;
    public static final byte RESPONSE_WITH_EXCEPTION_WITH_ATTACHMENTS = 3;
    public static final byte RESPONSE_VALUE_WITH_ATTACHMENTS = 4;
    public static final byte RESPONSE_NULL_VALUE_WITH_ATTACHMENTS = 5;
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];


    @Override
    protected Object decodeBody(Channel channel, InputStream is, byte[] header) throws IOException {
        byte flag = header[2];
        byte proto = (byte) (flag & SERIALIZATION_MASK);

        Serialization s = CodecSupport.getSerialization(channel.getUrl(), proto);
        long id = Bytes.bytes2long(header, 4);
        // response
        if ((flag & FLAG_REQUEST) == 0) {
            Response res = new Response(id);

            if ((flag & FLAG_EVENT) != 0) {
                res.setEvent(Response.HEARTBEAT_EVENT);
            }

            byte status = header[3];
            res.setStatus(status);
            if (status == Response.OK) {
                try {
                    Object data;
                    if (res.isHeartbeat()) {
                        data = decodeHeartbeatData(channel, deserialize(s, channel.getUrl(), is));
                    } else if (res.isEvent()) {
                        data = decodeEventData(channel, deserialize(s, channel.getUrl(), is));
                    } else {
                        DecodeableRpcResult result = new DecodeableRpcResult(channel, res, is,
                                (Invocation) getRequestData(id), proto);
                        result.decode();
                        data = result;
                    }
                    res.setResult(data);
                } catch (Throwable t) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Decode response failed: " + t.getMessage(), t);
                    }
                    res.setStatus(Response.CLIENT_ERROR);
                    res.setErrorMessage(StringUtils.toString(t));
                }
            } else {
                res.setErrorMessage(deserialize(s, channel.getUrl(), is).readUTF());
            }
            return res;
        } else {
            // request
            Request req = new Request(id);
            req.setVersion(Version.getVersion());
            req.setTwoWay((flag & FLAG_TWOWAY) != 0);
            if ((flag & FLAG_EVENT) != 0) {
                req.setEvent(Request.HEARTBEAT_EVENT);
            }

            try {
                Object data;
                if (req.isHeartbeat()) {
                    data = decodeHeartbeatData(channel, deserialize(s, channel.getUrl(), is));
                } else if (req.isEvent()) {
                    data = decodeRequestData(channel, deserialize(s, channel.getUrl(), is));
                } else {
                    DecodeableRpcInvocation inv = new DecodeableRpcInvocation(channel, req, is, flag);
                    inv.decode();
                    data = inv;
                }
                req.setData(data);
            } catch (Throwable t) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Decode request failed: " + t.getMessage(), t);
                }
                // bad request
                req.setBroken(true);
                req.setData(t);
            }
            return req;
        }
    }

    private ObjectInput deserialize(Serialization serialization, URL url, InputStream is)
            throws IOException {
        return serialization.deserialize(url, is);
    }
}
