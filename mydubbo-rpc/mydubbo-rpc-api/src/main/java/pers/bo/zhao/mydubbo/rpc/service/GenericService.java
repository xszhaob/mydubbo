package pers.bo.zhao.mydubbo.rpc.service;

/**
 * @author Bo.Zhao
 * @since 19/3/14
 */
public interface GenericService {

    Object $invoke(String method, String[] parameterTypes, Object[] args) throws GenericException;
}
