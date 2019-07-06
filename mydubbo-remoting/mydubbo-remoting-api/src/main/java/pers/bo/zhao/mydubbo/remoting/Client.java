package pers.bo.zhao.mydubbo.remoting;

/**
 * @author Bo.Zhao
 * @since 19/7/6
 */
public interface Client extends Endpoint, Channel, IdleSensible, Resetable {

    void reconnect() throws RemotingException;
}
