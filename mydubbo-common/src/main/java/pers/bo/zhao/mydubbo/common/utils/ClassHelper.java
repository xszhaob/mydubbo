package pers.bo.zhao.mydubbo.common.utils;

public class ClassHelper {

    private static final char PACKAGE_SEPARATOR_CHAR = '.';

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

    public static String simpleClassName(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        String className = clazz.getName();
        final int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        if (lastDotIdx > -1) {
            return className.substring(lastDotIdx + 1);
        }
        return className;
    }

    public static ClassLoader getCallerClassLoader(Class<?> caller) {
        return caller.getClassLoader();
    }
}
