package pers.bo.zhao.mydubbo.common;

/**
 * @author Bo.Zhao
 * @since 19/3/8
 */
public interface Node {

    URL getUrl();

    boolean isAvailable();

    void destroy();
}
