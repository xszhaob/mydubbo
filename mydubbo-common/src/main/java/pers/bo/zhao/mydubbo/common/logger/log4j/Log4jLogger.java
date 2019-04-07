package pers.bo.zhao.mydubbo.common.logger.log4j;

import org.apache.log4j.Level;
import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.support.FailSafeLogger;

/**
 * @author Bo.Zhao
 * @since 19/1/29
 */
public class Log4jLogger implements Logger {

    private static final String FQCN = FailSafeLogger.class.getName();

    private final org.apache.log4j.Logger logger;

    public Log4jLogger(org.apache.log4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void trace(String message) {
        logger.log(FQCN, Level.TRACE, message, null);
    }

    @Override
    public void trace(Throwable throwable) {
        logger.log(FQCN, Level.TRACE, getMessageOrNull(throwable), throwable);
    }

    @Override
    public void trace(String message, Throwable throwable) {
        logger.log(FQCN, Level.TRACE, message, throwable);
    }

    @Override
    public void debug(String message) {
        logger.log(FQCN, Level.DEBUG, message, null);
    }

    @Override
    public void debug(Throwable throwable) {
        logger.log(FQCN, Level.DEBUG, getMessageOrNull(throwable), null);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        logger.log(FQCN, Level.DEBUG, message, throwable);
    }

    @Override
    public void info(String message) {
        logger.log(FQCN, Level.INFO, message, null);
    }

    @Override
    public void info(Throwable throwable) {
        logger.log(FQCN, Level.INFO, getMessageOrNull(throwable), throwable);
    }

    @Override
    public void info(String message, Throwable throwable) {
        logger.log(FQCN, Level.INFO, message, throwable);
    }

    @Override
    public void warn(String message) {
        logger.log(FQCN, Level.WARN, message, null);
    }

    @Override
    public void warn(Throwable throwable) {
        logger.log(FQCN, Level.WARN, getMessageOrNull(throwable), throwable);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        logger.log(FQCN, Level.WARN, message, throwable);
    }

    @Override
    public void error(String message) {
        logger.log(FQCN, Level.ERROR, message, null);
    }

    @Override
    public void error(Throwable throwable) {
        logger.log(FQCN, Level.ERROR, getMessageOrNull(throwable), throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.log(FQCN, Level.ERROR, message, throwable);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabledFor(Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(Level.ERROR);
    }

    private String getMessageOrNull(Throwable throwable) {
        return throwable == null ? null : throwable.getMessage();
    }
}
