package pers.bo.zhao.mydubbo.common.logger.support;

import pers.bo.zhao.mydubbo.common.logger.Logger;

/**
 * @author Bo.Zhao
 * @since 19/1/29
 */
public class FailSafeLogger implements Logger {

    private final Logger logger;

    public FailSafeLogger(Logger logger) {
        this.logger = logger;
    }


    @Override
    public void trace(String message) {
        try {
            logger.trace(message);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void trace(Throwable throwable) {
        try {
            logger.trace(throwable);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void trace(String message, Throwable throwable) {
        try {
            logger.trace(message, throwable);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void debug(String message) {
        try {
            logger.debug(message);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void debug(Throwable throwable) {
        try {
            logger.debug(throwable);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void debug(String message, Throwable throwable) {
        try {
            logger.debug(message, throwable);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void info(String message) {
        try {
            logger.info(message);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void info(Throwable throwable) {
        try {
            logger.info(throwable);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void info(String message, Throwable throwable) {
        try {
            logger.info(message, throwable);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void warn(String message) {
        try {
            logger.warn(message);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void warn(Throwable throwable) {
        try {
            logger.warn(throwable);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void warn(String message, Throwable throwable) {
        try {
            logger.warn(message, throwable);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void error(String message) {
        try {
            logger.error(message);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void error(Throwable throwable) {
        try {
            logger.error(throwable);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void error(String message, Throwable throwable) {
        try {
            logger.error(message, throwable);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public boolean isTraceEnabled() {
        try {
            return logger.isTraceEnabled();
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public boolean isDebugEnabled() {
        try {
            return logger.isDebugEnabled();
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public boolean isInfoEnabled() {
        try {
            return logger.isInfoEnabled();
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public boolean isWarnEnabled() {
        try {
            return logger.isWarnEnabled();
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public boolean isErrorEnabled() {
        try {
            return logger.isErrorEnabled();
        } catch (Throwable e) {
            return false;
        }
    }
}
