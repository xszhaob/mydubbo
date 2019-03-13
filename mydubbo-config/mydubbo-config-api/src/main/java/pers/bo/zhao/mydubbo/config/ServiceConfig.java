package pers.bo.zhao.mydubbo.config;

import pers.bo.zhao.mydubbo.common.utils.NamedThreadFactory;
import pers.bo.zhao.mydubbo.common.utils.StringUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Bo.Zhao
 * @since 19/2/12
 */
public class ServiceConfig extends AbstractServiceConfig {

    private static final long serialVersionUID = 3033787999037024738L;

    private static final ScheduledExecutorService DELAY_EXPORT_EXECUTOR = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("MyDubboServiceDelayExporter", true));

    private String interfaceName;
    private Class<?> interfaceClass;

    private ProviderConfig provider;

    private transient volatile boolean exported;

    private transient volatile boolean unexported;

    public synchronized void export() {
        if (provider != null) {
            if (export == null) {
                export = provider.getExport();
            }
            if (delayed == null) {
                delayed = provider.getDelayed();
            }
        }

        if (export != null && !export) {
            return;
        }

        if (delayed > 0) {
            DELAY_EXPORT_EXECUTOR.schedule(this::doExport, delayed, TimeUnit.MILLISECONDS);
        } else {
            doExport();
        }
    }

    private synchronized void doExport() {
        if (unexported) {
            throw new IllegalStateException("MyDubbo already unexported!");
        }
        if (exported) {
            return;
        }
        exported = true;
        if (StringUtils.isEmpty(interfaceName)) {
            throw new IllegalStateException("<mydubbo:service interface=\"\" /> interface not allow null");
        }
        checkDefault();
        if (provider != null) {
            if (application == null) {
                application = provider.getApplication();
            }
//            if (module == null) {
//                module = provider.getModule();
//            }
            if (registries == null) {
                registries = provider.getRegistries();
            }
            if (protocols == null) {
                protocols = provider.getProtocols();
            }
        }
//        if (module != null) {
//            if (registries == null) {
//                registries = module
//            }
//        }
        if (application != null) {
            if (registries == null) {
                registries = application.getRegistries();
            }
        }


    }

    private void checkDefault() {
        if (provider == null) {
            provider = new ProviderConfig();
        }
        appendProperties(provider);
    }
}
