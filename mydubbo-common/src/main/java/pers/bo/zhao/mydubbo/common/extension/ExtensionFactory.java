package pers.bo.zhao.mydubbo.common.extension;

/**
 * @author Bo.Zhao
 * @since 19/1/26
 */
@SPI
public interface ExtensionFactory {

    <T> T getExtension(Class<T> type, String name);
}
