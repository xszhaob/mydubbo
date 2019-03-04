package pers.bo.zhao.mydubbo.container;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.extension.ExtensionLoader;
import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;
import pers.bo.zhao.mydubbo.common.utils.ConfigUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Bo.Zhao
 * @since 19/3/1
 */
public class Main {

    private static final String CONTAINER_KEY = "mydubbo.container";

    public static final String SHUTDOWN_HOOK_KEY = "mydubbo.shutdown.hook";


    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final ExtensionLoader<Container> LOADER = ExtensionLoader.getExtensionLoader(Container.class);

    private static final ReentrantLock LOCK = new ReentrantLock();

    private static final Condition STOP = LOCK.newCondition();

    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                String property = ConfigUtils.getProperty(CONTAINER_KEY, LOADER.getDefaultExtensionName());
                args = Constants.COMMA_SPLIT_PATTERN.split(property);
            }

            final List<Container> containers = new ArrayList<>(args.length);
            for (String arg : args) {
                containers.add(LOADER.getExtension(arg));
            }

            LOGGER.info("Use container type(" + Arrays.toString(args) + ") to run dubbo serivce.");

            if ("true".equals(System.getProperty(SHUTDOWN_HOOK_KEY))) {
                Runtime.getRuntime().addShutdownHook(new Thread("mydubbo-container-shutdown-hook") {
                    @Override
                    public void run() {
                        for (Container container : containers) {
                            try {
                                container.stop();
                                LOGGER.info("MyDubbo " + container.getClass().getSimpleName() + " stopped!");
                            } catch (Throwable t) {
                                LOGGER.error(t.getMessage(), t);
                            }
                            try {
                                LOCK.lock();
                                STOP.signal();
                            } finally {
                                LOCK.unlock();
                            }
                        }
                    }
                });
            }

            for (Container container : containers) {
                container.start();
                LOGGER.info("MyDubbo " + container.getClass().getSimpleName() + " started!");
            }
            System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " MyDubbo service server started!");
            // TODO: 19/3/1 这里为什么是RunTimeException？
        } catch (RuntimeException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
            System.exit(1);
        }

        try {
            LOCK.lock();
            STOP.await();
        } catch (InterruptedException e) {
            LOGGER.warn("MyDubbo service server stopped, interrupted by other thread!", e);
        } finally {
            LOCK.unlock();
        }
    }
}
