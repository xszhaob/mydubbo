package org.apache.mydubbo.common.serialize.hessian2;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.serialize.ObjectInput;
import pers.bo.zhao.mydubbo.serialize.ObjectOutput;
import pers.bo.zhao.mydubbo.serialize.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Hessian2Serialization implements Serialization {

    public static final byte ID = 2;

    @Override
    public byte getContentTypeId() {
        return ID;
    }

    @Override
    public String getContentType() {
        return "x-application/hessian2";
    }

    @Override
    public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
        return new Hessian2ObjectOutput(out);
    }

    @Override
    public ObjectInput deserialize(URL url, InputStream is) throws IOException {
        return new Hessian2ObjectInput(is);
    }

}
