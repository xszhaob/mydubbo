package pers.bo.zhao.mydubbo.common.utils;

/**
 * @author Bo.Zhao
 * @since 19/1/26
 */
public class ClassUtils {

    /**
     * 获取类加载器
     */
    public static ClassLoader getClassLoader(Class<?> clazz) {
        ClassLoader classLoader = null;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ignore) {

        }
        if (classLoader == null) {
            classLoader = clazz.getClassLoader();
            try {
                if (classLoader == null) {
                    classLoader = ClassLoader.getSystemClassLoader();
                }
            } catch (Throwable ignore) {

            }
        }
        return classLoader;
    }
}
