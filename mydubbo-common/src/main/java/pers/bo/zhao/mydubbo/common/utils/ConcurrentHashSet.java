package pers.bo.zhao.mydubbo.common.utils;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Bo.Zhao
 * @since 19/1/28
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Serializable {

    private static final long serialVersionUID = -8672117787651310382L;

    private static final Object PERSENT = new Object();

    private final Map<E, Object> map;


    public ConcurrentHashSet() {
        this.map = new ConcurrentHashMap<>();
    }

    public ConcurrentHashSet(int initCapacity) {
        this.map = new ConcurrentHashMap<>(initCapacity);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean add(E e) {
        return map.put(e, PERSENT) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) == PERSENT;
    }

    @Override
    public void clear() {
        map.clear();
    }
}
