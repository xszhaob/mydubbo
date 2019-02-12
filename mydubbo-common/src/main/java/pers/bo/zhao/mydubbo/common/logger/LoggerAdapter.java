package pers.bo.zhao.mydubbo.common.logger;

import java.io.File;

/**
 * @author Bo.Zhao
 * @since 19/1/29
 */
public interface LoggerAdapter {

    Logger getLogger(Class<?> key);

    Logger getLogger(String key);

    Level getLevel();

    void setLevel(Level level);

    File getFile();

    void setFile(File file);
}
