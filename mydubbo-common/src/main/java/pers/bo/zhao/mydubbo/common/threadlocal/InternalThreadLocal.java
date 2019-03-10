package pers.bo.zhao.mydubbo.common.threadlocal;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class InternalThreadLocal<T> {

    private static final int variablesToRemoveIndex = InternalThreadLocalMap.nextVariableIndex();

    private final int index;

    public InternalThreadLocal() {
        this.index = InternalThreadLocalMap.nextVariableIndex();
    }

    @SuppressWarnings("unchecked")
    public static void removeAll() {
        InternalThreadLocalMap localMap = InternalThreadLocalMap.getIfSet();
        if (localMap == null) {
            return;
        }

        try {
            Object v = localMap.indexedVariable(variablesToRemoveIndex);
            if (v != null && v != InternalThreadLocalMap.UNSET) {
                Set<InternalThreadLocal<?>> variablesToRemove = (Set<InternalThreadLocal<?>>) v;
                InternalThreadLocal<?>[] variablesToRemoveArray = variablesToRemove.toArray(new InternalThreadLocal[0]);
                for (InternalThreadLocal<?> itl : variablesToRemoveArray) {
                    itl.remove(localMap);
                }
            }
        } finally {
            InternalThreadLocalMap.remove();
        }
    }

    public static int size() {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();

        if (threadLocalMap == null) {
            return 0;
        } else {
            return threadLocalMap.size();
        }
    }

    public static void destroy() {
        InternalThreadLocalMap.destory();
    }

    @SuppressWarnings("unchecked")
    private static void addToVariablesToRemove(InternalThreadLocalMap threadLocalMap, InternalThreadLocal<?> variable) {
        Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
        Set<InternalThreadLocal<?>> variablesToRemove;

        if (v == null || v == InternalThreadLocalMap.UNSET) {
            variablesToRemove = Collections.newSetFromMap(new IdentityHashMap<>());
            threadLocalMap.setIndexedVariable(variablesToRemoveIndex, variablesToRemove);
        } else {
            variablesToRemove = (Set<InternalThreadLocal<?>>) v;
        }

        variablesToRemove.add(variable);
    }


    @SuppressWarnings("unchecked")
    private void removeFromVariablesToRemove(InternalThreadLocalMap threadLocalMap, InternalThreadLocal<T> variable) {
        Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);

        if (v == null || v == InternalThreadLocalMap.UNSET) {
            return;
        }

        Set<InternalThreadLocal<?>> variableToRemove = (Set<InternalThreadLocal<?>>) v;
        variableToRemove.remove(variable);
    }

    @SuppressWarnings("unchecked")
    public final T get() {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();

        Object v = threadLocalMap.indexedVariable(index);
        if (v != InternalThreadLocalMap.UNSET) {
            return (T) v;
        }
        return initialize(threadLocalMap);
    }

    private T initialize(InternalThreadLocalMap threadLocalMap) {
        T v = null;

        try {
            v = initialValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        threadLocalMap.setIndexedVariable(index, v);
        addToVariablesToRemove(threadLocalMap, this);
        return v;
    }


    public final void set(T v) {
        if (v == null || v == InternalThreadLocalMap.UNSET) {
            remove();
        } else {
            InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
            if (threadLocalMap.setIndexedVariable(index, v)) {
                addToVariablesToRemove(threadLocalMap, this);
            }
        }
    }

    public final void remove() {
        remove(InternalThreadLocalMap.getIfSet());
    }

    @SuppressWarnings("unchecked")
    public final void remove(InternalThreadLocalMap threadLocalMap) {
        if (threadLocalMap == null) {
            return;
        }

        Object o = threadLocalMap.removeIndexedVariable(index);
        removeFromVariablesToRemove(threadLocalMap, this);

        if (o != InternalThreadLocalMap.UNSET) {
            try {
                onRemoval((T) o);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Returns the initial value for this thread-local variable.
     */
    protected T initialValue() throws Exception {
        return null;
    }

    /**
     * Invoked when this thread local variable is removed by {@link #remove()}.
     */
    protected void onRemoval(@SuppressWarnings("unused") T value) throws Exception {
    }
}
