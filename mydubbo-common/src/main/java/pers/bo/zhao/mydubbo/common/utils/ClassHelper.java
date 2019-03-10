package pers.bo.zhao.mydubbo.common.utils;

public class ClassHelper {

    public static ClassLoader getClassLoader() {
        return getClassLoader(ClassHelper.class);
    }


    public static ClassLoader getClassLoader(Class<?> clazz) {
        ClassLoader cl = null;

        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ignore) {
        }
        if (cl == null) {
            cl = clazz.getClassLoader();
        }
        return cl;
    }
}
