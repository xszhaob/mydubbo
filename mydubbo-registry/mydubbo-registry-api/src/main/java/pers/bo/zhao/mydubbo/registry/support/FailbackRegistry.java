package pers.bo.zhao.mydubbo.registry.support;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.timer.HashedWheelTimer;
import pers.bo.zhao.mydubbo.common.utils.ConcurrentHashSet;
import pers.bo.zhao.mydubbo.common.utils.NamedThreadFactory;
import pers.bo.zhao.mydubbo.registry.NotifyListener;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public abstract class FailbackRegistry extends AbstractRegistry {
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("DubboRegistryFailedRetryTimer", true));

    private final Set<URL> failedRegistered = new ConcurrentHashSet<>();

    private final Set<URL> failedUnregistered = new ConcurrentHashSet<>();

    private final ConcurrentMap<URL, Set<NotifyListener>> failedSubscribed = new ConcurrentHashMap<>();

    private final ConcurrentMap<URL, Set<NotifyListener>> failedUnsubscribed = new ConcurrentHashMap<>();

    private final ConcurrentMap<URL, Map<NotifyListener, List<URL>>> failedNotified = new ConcurrentHashMap<>();
    private final int retryPeriod;

    private final HashedWheelTimer retryTimer;

    public FailbackRegistry(URL url) {
        super(url);
        this.retryPeriod = url.getParameter(Constants.REGISTRY_RETRY_PERIOD_KEY, Constants.DEFAULT_REGISTRY_RETRY_PERIOD);
        this.retryTimer = new HashedWheelTimer(new NamedThreadFactory("MyDubboRegistryRetryTimer"), retryPeriod, TimeUnit.MILLISECONDS, 128);

    }


    public Set<URL> getFailedRegistered() {
        return failedRegistered;
    }

    public Set<URL> getFailedUnregistered() {
        return failedUnregistered;
    }

    public ConcurrentMap<URL, Set<NotifyListener>> getFailedSubscribed() {
        return failedSubscribed;
    }

    public ConcurrentMap<URL, Set<NotifyListener>> getFailedUnsubscribed() {
        return failedUnsubscribed;
    }

    public ConcurrentMap<URL, Map<NotifyListener, List<URL>>> getFailedNotified() {
        return failedNotified;
    }

    private void addFailedSubscribed(URL url, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(url);
        if (listeners == null) {
            failedSubscribed.putIfAbsent(url, new ConcurrentHashSet<NotifyListener>());
            listeners = failedSubscribed.get(url);
        }
        listeners.add(listener);
    }

    private void removeFailedSubscribed(URL url, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        listeners = failedUnsubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        Map<NotifyListener, List<URL>> notified = failedNotified.get(url);
        if (notified != null) {
            notified.remove(listener);
        }
    }


    @Override
    public void register(URL url) {
        super.register(url);
        failedRegistered.remove(url);
        failedUnregistered.remove(url);

        try {
            doRegister(url);
        } catch (Exception e) {
            Throwable t = e;

            boolean check = getUrl().getParameter(Constants.CHECK_KEY, true)
                    && url.getParameter(Constants.CHECK_KEY, true)
                    && !Constants.CONSUMER_PROTOCOL.equals(url.getProtocol());
            boolean skipFailback = t instanceof SkipFailbackWrapperException;

            if (check || skipFailback) {
                if (skipFailback) {
                    t = t.getCause();
                }
                throw new IllegalStateException("Failed to register " + url +
                        " to registry " + getUrl().getAddress() +
                        ", cause: " + t.getMessage(), t);
            } else {
                LOGGER.error("Failed to register " + url + ", waiting for retry, cause: " + t.getMessage(), t);
            }

            failedRegistered.add(url);
        }
    }

    @Override
    public void unregister(URL url) {
        super.unregister(url);
        failedRegistered.remove(url);
        failedUnregistered.remove(url);
        try {
            // Sending a cancellation request to the server side
            doUnregister(url);
        } catch (Exception e) {
            Throwable t = e;

            // If the startup detection is opened, the Exception is thrown directly.
            boolean check = getUrl().getParameter(Constants.CHECK_KEY, true)
                    && url.getParameter(Constants.CHECK_KEY, true)
                    && !Constants.CONSUMER_PROTOCOL.equals(url.getProtocol());
            boolean skipFailback = t instanceof SkipFailbackWrapperException;
            if (check || skipFailback) {
                if (skipFailback) {
                    t = t.getCause();
                }
                throw new IllegalStateException("Failed to unregister " + url + " to registry " + getUrl().getAddress() + ", cause: " + t.getMessage(), t);
            } else {
                LOGGER.error("Failed to uregister " + url + ", waiting for retry, cause: " + t.getMessage(), t);
            }

            // Record a failed registration request to a failed list, retry regularly
            failedUnregistered.add(url);
        }
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        super.subscribe(url, listener);
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        super.unsubscribe(url, listener);
    }


    @Override
    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }

        try {
            doNotify(url, listener, urls);
        } catch (Exception e) {
            addFailedNotified(url, listener, urls);
            LOGGER.error("Failed to notify for subscribe " + url + ", waiting for retry, cause: " + e.getMessage(), e);
        }
    }

    /**
     * 利用timer框架实现重试
     */
    private void addFailedNotified(URL url, NotifyListener listener, List<URL> urls) {

    }

    private void doNotify(URL url, NotifyListener listener, List<URL> urls) {
        super.notify(url, listener, urls);
    }

    protected void retry() {

    }


    // ==== Template method ====

    protected abstract void doRegister(URL url);

    protected abstract void doUnregister(URL url);

    protected abstract void doSubscribe(URL url, NotifyListener listener);

    protected abstract void doUnsubscribe(URL url, NotifyListener listener);


}
