package pers.bo.zhao.mydubbo.config;

import pers.bo.zhao.mydubbo.common.utils.NamedThreadFactory;

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

    private ProviderConfig provider;

    public void export() {
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

    private void doExport() {

    }
}
