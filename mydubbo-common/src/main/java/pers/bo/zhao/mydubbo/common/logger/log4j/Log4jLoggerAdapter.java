package pers.bo.zhao.mydubbo.common.logger.log4j;

import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import pers.bo.zhao.mydubbo.common.logger.Level;
import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerAdapter;

import java.io.File;
import java.util.Enumeration;

/**
 * @author Bo.Zhao
 * @since 19/1/29
 */
public class Log4jLoggerAdapter implements LoggerAdapter {

    private File file;

    @SuppressWarnings("unchecked")
    public Log4jLoggerAdapter() {
        try {
            org.apache.log4j.Logger rootLogger = LogManager.getRootLogger();
            if (rootLogger != null) {
                Enumeration<Appendable> appenders = rootLogger.getAllAppenders();
                if (appenders != null) {
                    while (appenders.hasMoreElements()) {
                        Appendable appendable = appenders.nextElement();
                        if (appendable instanceof FileAppender) {
                            String fileName = ((FileAppender) appendable).getFile();
                            this.file = new File(fileName);
                            break;
                        }
                    }
                }
            }
        } catch (Throwable ignore) {
        }
    }

    @Override
    public Logger getLogger(Class<?> key) {
        return new Log4jLogger(LogManager.getLogger(key));
    }

    @Override
    public Logger getLogger(String key) {
        return new Log4jLogger(LogManager.getLogger(key));
    }

    @Override
    public Level getLevel() {
        return convertFromLog4jLevel(LogManager.getRootLogger().getLevel());
    }

    private static Level convertFromLog4jLevel(org.apache.log4j.Level level) {
        if (level == org.apache.log4j.Level.ALL) {
            return Level.ALL;
        } else if (level == org.apache.log4j.Level.TRACE) {
            return Level.TRACE;
        } else if (level == org.apache.log4j.Level.DEBUG) {
            return Level.DEBUG;
        } else if (level == org.apache.log4j.Level.INFO) {
            return Level.INFO;
        } else if (level == org.apache.log4j.Level.WARN) {
            return Level.WARN;
        } else if (level == org.apache.log4j.Level.ERROR) {
            return Level.ERROR;
        }
        return Level.OFF;
    }


    private static org.apache.log4j.Level convertToLog4jLevel(Level level) {
        if (level == Level.ALL) {
            return org.apache.log4j.Level.ALL;
        } else if (level == Level.TRACE) {
            return org.apache.log4j.Level.TRACE;
        } else if (level == Level.DEBUG) {
            return org.apache.log4j.Level.DEBUG;
        } else if (level == Level.INFO) {
            return org.apache.log4j.Level.INFO;
        } else if (level == Level.WARN) {
            return org.apache.log4j.Level.WARN;
        } else if (level == Level.ERROR) {
            return org.apache.log4j.Level.ERROR;
        }
        return org.apache.log4j.Level.OFF;
    }

    @Override
    public void setLevel(Level level) {
        LogManager.getRootLogger().setLevel(convertToLog4jLevel(level));
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void setFile(File file) {

    }
}
