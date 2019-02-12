package pers.bo.zhao.mydubbo.common.extension.factory;

import pers.bo.zhao.mydubbo.common.extension.Adaptive;
import pers.bo.zhao.mydubbo.common.extension.ExtensionFactory;
import pers.bo.zhao.mydubbo.common.extension.ExtensionLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Bo.Zhao
 * @since 19/2/11
 */
@Adaptive
public class AdaptiveExtensionFactory implements ExtensionFactory {

    private List<ExtensionFactory> factories;

    public AdaptiveExtensionFactory() {
        ExtensionLoader<ExtensionFactory> loader = ExtensionLoader.getExtensionLoader(ExtensionFactory.class);
        List<ExtensionFactory> list = new ArrayList<>();
        for (String extension : loader.getSupportedExtensions()) {
            list.add(loader.getExtension(extension));
        }
        this.factories = Collections.unmodifiableList(list);
    }

    @Override
    public <T> T getExtension(Class<T> type, String name) {
        for (ExtensionFactory factory : factories) {
            T extension = factory.getExtension(type, name);
            if (extension != null) {
                return extension;
            }
        }
        return null;
    }
}
