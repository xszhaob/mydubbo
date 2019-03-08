package pers.bo.zhao.mydubbo.rpc;

/**
 * @author Bo.Zhao
 * @since 19/3/8
 */
public class RpcException extends RuntimeException {

    public RpcException() {
        super();
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }
}
