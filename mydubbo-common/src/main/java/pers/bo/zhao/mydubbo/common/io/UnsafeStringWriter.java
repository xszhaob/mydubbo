package pers.bo.zhao.mydubbo.common.io;

import java.io.IOException;
import java.io.Writer;

/**
 * 非线程安全的StringWriter
 *
 * @author Bo.Zhao
 * @since 19/2/11
 */
public class UnsafeStringWriter extends Writer {
    private StringBuilder buffer;


    public UnsafeStringWriter() {
        this.lock = this.buffer = new StringBuilder();
    }


    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (off < 0 || off > cbuf.length || len < 0 || (off + len) > cbuf.length || (off + len) < 0) {
            throw new IndexOutOfBoundsException();
        }
        buffer.append(cbuf, off, len);
    }

    @Override
    public void write(int c) throws IOException {
        buffer.append((char) c);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        write(cbuf, 0, cbuf.length);
    }

    @Override
    public void write(String str) throws IOException {
        buffer.append(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        buffer.append(str, off, off + len);
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        if (csq == null) {
            write("null");
        } else {
            write(csq.toString());
        }
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        if (csq == null) {
            csq = "null";
        }
        CharSequence charSequence = csq.subSequence(start, end);
        write(charSequence.toString());
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        buffer.append(c);
        return this;
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public String toString() {
        return buffer.toString();
    }


}
