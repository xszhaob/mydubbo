package pers.bo.zhao.mydubbo.registry.retry;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;
import pers.bo.zhao.mydubbo.common.timer.Timeout;
import pers.bo.zhao.mydubbo.common.timer.Timer;
import pers.bo.zhao.mydubbo.common.timer.TimerTask;
import pers.bo.zhao.mydubbo.common.utils.StringUtils;
import pers.bo.zhao.mydubbo.registry.support.FailbackRegistry;

import java.util.concurrent.TimeUnit;

/**
 * @author Bo.Zhao
 * @since 19/4/30
 */
public abstract class AbstractRetryTask implements TimerTask {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final URL url;

    protected final FailbackRegistry registry;

    final long retryPeriod;

    private final int retryTimes;

    private final String taskName;

    private int times = 1;

    private volatile boolean cancel = false;


    public AbstractRetryTask(URL url, FailbackRegistry registry, String taskName) {
        if (url == null || StringUtils.isEmpty(taskName)) {
            throw new IllegalArgumentException("url or retryName illegal");
        }
        this.url = url;
        this.registry = registry;
        this.taskName = taskName;
        cancel = false;
        this.retryPeriod = url.getParameter(Constants.REGISTRY_RETRY_PERIOD_KEY, Constants.DEFAULT_REGISTRY_RETRY_PERIOD);
        this.retryTimes = url.getParameter(Constants.REGISTRY_RETRY_TIMES_KEY, Constants.DEFAULT_REGISTRY_RETRY_TIMES);
    }

    public void cancel() {
        this.cancel = true;
    }

    public boolean isCancel() {
        return cancel;
    }

    protected void reput(Timeout timeout, long tick) {
        if (timeout == null) {
            throw new IllegalArgumentException("timeout = null!");
        }

        Timer timer = timeout.timer();
        if (timer.isStop() || timeout.isCancelled() || isCancel()) {
            return;
        }

        times++;
        timer.newTimeout(timeout.task(), tick, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        // 判断是否停止
        if (timeout.isCancelled() || timeout.timer().isStop() || isCancel()) {
            return;
        }
        if (times > retryTimes) {
            // reach the most times of retry.
            logger.warn("Final failed to execute task " + taskName + ", url: " + url + ", retry " + retryTimes + " times.");
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info(taskName + " : " + url);
        }
        try {
            doRetry(url, registry, timeout);
        } catch (Throwable t) {
            logger.warn("Failed to execute task " + taskName + ", url: " + url + ", waiting for again, cause:" + t.getMessage(), t);
            reput(timeout, retryPeriod);
        }
    }

    protected abstract void doRetry(URL url, FailbackRegistry registry, Timeout timeout);
}
