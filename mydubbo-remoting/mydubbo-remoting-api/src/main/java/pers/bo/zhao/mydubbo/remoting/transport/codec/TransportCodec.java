//package pers.bo.zhao.mydubbo.remoting.transport.codec;
//
//import pers.bo.zhao.mydubbo.common.utils.StringUtils;
//import pers.bo.zhao.mydubbo.remoting.Channel;
//import pers.bo.zhao.mydubbo.remoting.buffer.ChannelBuffer;
//import pers.bo.zhao.mydubbo.remoting.buffer.ChannelBufferInputStream;
//import pers.bo.zhao.mydubbo.remoting.buffer.ChannelBufferOutputStream;
//import pers.bo.zhao.mydubbo.remoting.transport.AbstractCodec;
//import pers.bo.zhao.mydubbo.serialize.Cleanable;
//import pers.bo.zhao.mydubbo.serialize.ObjectInput;
//import pers.bo.zhao.mydubbo.serialize.ObjectOutput;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//public class TransportCodec extends AbstractCodec {
//
//    @Override
//    public void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException {
//        OutputStream output = new ChannelBufferOutputStream(buffer);
//        ObjectOutput objectOutput = getSerialization(channel).serialize(channel.getUrl(), output);
//        encodeData(channel, objectOutput, message);
//        objectOutput.flushBuffer();
//        if (objectOutput instanceof Cleanable) {
//            ((Cleanable) objectOutput).cleanup();
//        }
//    }
//
//    @Override
//    public Object decode(Channel channel, ChannelBuffer buffer) throws IOException {
//        InputStream input = new ChannelBufferInputStream(buffer);
//        ObjectInput objectInput = getSerialization(channel).deserialize(channel.getUrl(), input);
//        Object object = decodeData(channel, objectInput);
//        if (objectInput instanceof Cleanable) {
//            ((Cleanable) objectInput).cleanup();
//        }
//        return object;
//    }
//
//    protected void encodeData(Channel channel, ObjectOutput output, Object message) throws IOException {
//        encodeData(output, message);
//    }
//
//    protected Object decodeData(Channel channel, ObjectInput input) throws IOException {
//        return decodeData(input);
//    }
//
//    protected void encodeData(ObjectOutput output, Object message) throws IOException {
//        output.writeObject(message);
//    }
//
//    protected Object decodeData(ObjectInput input) throws IOException {
//        try {
//            return input.readObject();
//        } catch (ClassNotFoundException e) {
//            throw new IOException("ClassNotFoundException: " + StringUtils.toString(e));
//        }
//    }
//}
