package pers.bo.zhao.mydubbo.common.utils;

/**
 * @author Bo.Zhao
 * @since 19/1/26
 */
public class Holder<T> {

    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
