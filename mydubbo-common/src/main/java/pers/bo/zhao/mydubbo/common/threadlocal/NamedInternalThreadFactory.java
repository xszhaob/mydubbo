package pers.bo.zhao.mydubbo.common.threadlocal;

import pers.bo.zhao.mydubbo.common.utils.NamedThreadFactory;

public class NamedInternalThreadFactory extends NamedThreadFactory {


    public NamedInternalThreadFactory() {
        super();
    }

    public NamedInternalThreadFactory(String prefix) {
        super(prefix);
    }

    public NamedInternalThreadFactory(String prefix, boolean mDaemon) {
        super(prefix, mDaemon);
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = prefix + mThreadNum.incrementAndGet();
        InternalThread thread = new InternalThread(mGroup, r, name, 0);
        thread.setDaemon(mDaemon);
        return thread;
    }
}
