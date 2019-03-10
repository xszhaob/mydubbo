package pers.bo.zhao.mydubbo.common.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    protected static final AtomicInteger POOL_SEQ = new AtomicInteger(1);

    protected static final AtomicInteger mThreadNum = new AtomicInteger(1);

    protected final String prefix;

    protected final boolean mDaemon;

    protected final ThreadGroup mGroup;


    public NamedThreadFactory() {
        this("pool-" + POOL_SEQ.incrementAndGet(), false);
    }

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    public NamedThreadFactory(String prefix, boolean mDaemon) {
        this.prefix = prefix + "-thread-";
        this.mDaemon = mDaemon;
        SecurityManager s = System.getSecurityManager();
        this.mGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = prefix + mThreadNum.incrementAndGet();
        Thread thread = new Thread(mGroup, r, name, 0);
        thread.setDaemon(mDaemon);
        return thread;
    }


    public ThreadGroup getThreadGroup() {
        return mGroup;
    }
}
