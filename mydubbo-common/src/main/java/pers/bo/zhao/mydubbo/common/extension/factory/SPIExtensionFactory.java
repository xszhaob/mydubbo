package pers.bo.zhao.mydubbo.common.extension.factory;

import pers.bo.zhao.mydubbo.common.extension.ExtensionFactory;
import pers.bo.zhao.mydubbo.common.extension.ExtensionLoader;
import pers.bo.zhao.mydubbo.common.extension.SPI;
import pers.bo.zhao.mydubbo.common.utils.CollectionUtils;

/**
 * @author Bo.Zhao
 * @since 19/2/11
 */
public class SPIExtensionFactory implements ExtensionFactory {

    @Override
    public <T> T getExtension(Class<T> type, String name) {
        if (type.isInterface() && type.isAnnotationPresent(SPI.class)) {
            ExtensionLoader<T> loader = ExtensionLoader.getExtensionLoader(type);
            if (CollectionUtils.isNotEmpty(loader.getSupportedExtensions())) {
                return loader.getAdaptiveExtension();
            }
        }
        return null;
    }
}
