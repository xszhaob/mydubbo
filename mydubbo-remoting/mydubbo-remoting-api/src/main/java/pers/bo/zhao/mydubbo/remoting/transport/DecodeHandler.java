package pers.bo.zhao.mydubbo.remoting.transport;

import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;
import pers.bo.zhao.mydubbo.remoting.Channel;
import pers.bo.zhao.mydubbo.remoting.ChannelHandler;
import pers.bo.zhao.mydubbo.remoting.Decodeable;
import pers.bo.zhao.mydubbo.remoting.RemotingException;
import pers.bo.zhao.mydubbo.remoting.exchange.Request;
import pers.bo.zhao.mydubbo.remoting.exchange.Response;

public class DecodeHandler extends AbstractChannelHandlerDelegate {
    private static final Logger log = LoggerFactory.getLogger(DecodeHandler.class);


    public DecodeHandler(ChannelHandler handler) {
        super(handler);
    }


    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        if (message instanceof Decodeable) {
            decode(message);
        }
        if (message instanceof Request) {
            decode(((Request) message).getData());
        }
        if (message instanceof Response) {
            decode(((Response) message).getResult());
        }
        handler.received(channel, message);
    }

    private void decode(Object message) {
        if (message instanceof Decodeable) {
            try {
                ((Decodeable) message).decode();
                if (log.isDebugEnabled()) {
                    log.debug("Decode decodeable message " + message.getClass().getName());
                }
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn("Call Decodeable.decode failed: " + e.getMessage(), e);
                }
            }
        }
    }
}
