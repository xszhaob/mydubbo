package pers.bo.zhao.mydubbo.common.threadpool;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.extension.Adaptive;
import pers.bo.zhao.mydubbo.common.extension.SPI;
import pers.bo.zhao.mydubbo.common.threadpool.support.FixedThreadPool;

import java.util.concurrent.Executor;

@SPI(FixedThreadPool.NAME)
public interface ThreadPool {

    @Adaptive(Constants.THREADPOOL_KEY)
    Executor getExecutor(URL url);
}
