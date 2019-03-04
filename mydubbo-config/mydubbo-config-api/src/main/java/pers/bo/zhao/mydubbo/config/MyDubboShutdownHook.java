package pers.bo.zhao.mydubbo.config;

import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bo.Zhao
 * @since 19/3/3
 */
public class MyDubboShutdownHook extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyDubboShutdownHook.class);

    private static final MyDubboShutdownHook myDubboShutdownHook = new MyDubboShutdownHook("MyDubboShutdownHook");

    private final AtomicBoolean destroyed;

    public static MyDubboShutdownHook getDubboShutdownHook() {
        return myDubboShutdownHook;
    }

    public MyDubboShutdownHook(String name) {
        super(name);
        destroyed = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        if (LOGGER.isInfoEnable()) {
            LOGGER.info("Run myDubbo shutdown hook now.");
        }

    }


    public void destroyedAll() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }

        // destroyed all the registries

        // destroyed all the protocols
    }
}
