package pers.bo.zhao.mydubbo.registry.support;

public class SkipFailbackWrapperException extends RuntimeException {

    public SkipFailbackWrapperException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // do nothing
        return null;
    }
}
