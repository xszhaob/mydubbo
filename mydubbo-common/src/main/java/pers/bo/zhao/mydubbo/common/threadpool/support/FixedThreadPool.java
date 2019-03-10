package pers.bo.zhao.mydubbo.common.threadpool.support;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.threadpool.ThreadPool;

import java.util.concurrent.*;

public class FixedThreadPool implements ThreadPool {

    public static final String NAME = "fixed";

    @Override
    public Executor getExecutor(URL url) {
        String name = url.getParameter(Constants.THREAD_NAME_KEY, Constants.DEFAULT_THREAD_NAME);
        int threads = url.getParameter(Constants.THREADS_KEY, Constants.DEFAULT_THREADS);
        int queues = url.getParameter(Constants.QUEUES_KEY, Constants.DEFAULT_QUEUES);

        return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS,
                queues == 0 ? new SynchronousQueue<>() :
                        (queues > 0 ? new LinkedBlockingDeque<>(queues)
                                // TODO: 2019/3/9 等待完善
                                : new LinkedBlockingDeque<>()));
    }
}
