package pers.bo.zhao.mydubbo.common.logger;

/**
 * @author Bo.Zhao
 * @since 19/1/29
 */
public interface Logger {

    void trace(String message);

    void trace(Throwable throwable);

    void trace(String message, Throwable throwable);


    void debug(String message);

    void debug(Throwable throwable);

    void debug(String message, Throwable throwable);


    void info(String message);

    void info(Throwable throwable);

    void info(String message, Throwable throwable);


    void warn(String message);

    void warn(Throwable throwable);

    void warn(String message, Throwable throwable);


    void error(String message);

    void error(Throwable throwable);

    void error(String message, Throwable throwable);


    boolean isTraceEnabled();

    boolean isDebugEnabled();

    boolean isInfoEnabled();

    boolean isWarnEnabled();

    boolean isErrorEnabled();
}
