package pers.bo.zhao.mydubbo.common.logger;

import pers.bo.zhao.mydubbo.common.logger.log4j.Log4jLoggerAdapter;
import pers.bo.zhao.mydubbo.common.logger.support.FailSafeLogger;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Bo.Zhao
 * @since 19/1/29
 */
public class LoggerFactory {

    private static final ConcurrentMap<String, FailSafeLogger> LOGGERS = new ConcurrentHashMap<>();

    private static volatile LoggerAdapter LOGGER_ADAPTER;

    static {
//        String property = System.getProperty("dubbo.application.logger");
//        if ("log4j".equals(property)) {
            setLoggerAdapter(new Log4jLoggerAdapter());
//        }
    }


    private static void setLoggerAdapter(LoggerAdapter loggerAdapter) {
        if (loggerAdapter != null) {
            Logger logger = loggerAdapter.getLogger(LoggerFactory.class);
            logger.info("using logger:" + logger.getClass().getName());
            LOGGER_ADAPTER = loggerAdapter;
        }
    }

    public static Logger getLogger(Class<?> key) {
        FailSafeLogger failSafeLogger = LOGGERS.get(key.getName());
        if (failSafeLogger == null) {
            LOGGERS.putIfAbsent(key.getName(), new FailSafeLogger(LOGGER_ADAPTER.getLogger(key)));
            failSafeLogger = LOGGERS.get(key.getName());
        }
        return failSafeLogger;
    }

    public static Logger getLogger(String key) {
        FailSafeLogger logger = LOGGERS.get(key);
        if (logger == null) {
            LOGGERS.putIfAbsent(key, new FailSafeLogger(LOGGER_ADAPTER.getLogger(key)));
            logger = LOGGERS.get(key);
        }
        return logger;
    }


    public static Level getLevel() {
        return LOGGER_ADAPTER.getLevel();
    }

    public static void setLevel(Level level) {
        LOGGER_ADAPTER.setLevel(level);
    }

    public static File getFile() {
        return LOGGER_ADAPTER.getFile();
    }
}
