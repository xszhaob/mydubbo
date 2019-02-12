package pers.bo.zhao.mydubbo.config.spring.extension;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import pers.bo.zhao.mydubbo.common.extension.ExtensionFactory;
import pers.bo.zhao.mydubbo.common.extension.SPI;
import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;
import pers.bo.zhao.mydubbo.common.utils.ConcurrentHashSet;

import java.util.Set;

/**
 * @author Bo.Zhao
 * @since 19/2/11
 */
public class SpringExtensionFactory implements ExtensionFactory {

    private static final Logger logger = LoggerFactory.getLogger(SpringExtensionFactory.class);

    private static final Set<ApplicationContext> contexts = new ConcurrentHashSet<>();

    public static void addApplicationContext(ApplicationContext context) {
        contexts.add(context);
    }

    public static void removeApplicationContext(ApplicationContext context) {
        contexts.remove(context);
    }

    public static Set<ApplicationContext> getContexts() {
        return contexts;
    }

    public static void clearContexts() {
        contexts.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExtension(Class<T> type, String name) {
        // SPI的类有SPIExtensionFactory处理
        if (type.isInterface() && type.isAnnotationPresent(SPI.class)) {
            return null;
        }

        for (ApplicationContext context : contexts) {
            if (context.containsBean(name)) {
                Object bean = context.getBean(name);
                if (type.isInstance(bean)) {
                    return (T) bean;
                }
            }
        }

        logger.warn("No spring extension(bean) named:" + name + ", try to find an extension(bean) of type " + type.getName());


        for (ApplicationContext context : contexts) {
            try {
                return context.getBean(type);
            } catch (NoSuchBeanDefinitionException e) {
                if (logger.isDebugEnable()) {
                    logger.debug("Error when get spring extension(bean) for type: " + type, e);
                }
            }
        }
        logger.warn("No spring extension(bean) named:" + name + ", type:" + type.getName() + " found, stop get bean.");

        return null;
    }
}
