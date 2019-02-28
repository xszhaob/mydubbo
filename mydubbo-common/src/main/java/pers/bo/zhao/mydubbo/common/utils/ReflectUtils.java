package pers.bo.zhao.mydubbo.common.utils;

/**
 * @author Bo.Zhao
 * @since 19/2/28
 */
public class ReflectUtils {

    public static boolean isGetMethod(String methodName) {
        return methodName.startsWith("get") || methodName.startsWith("is");
    }

    public static Class<?> getBoxedClass(Class<?> c) {
        if (c == int.class) {
            c = Integer.class;
        } else if (c == boolean.class) {
            c = Boolean.class;
        } else if (c == long.class) {
            c = Long.class;
        } else if (c == float.class) {
            c = Float.class;
        } else if (c == double.class) {
            c = Double.class;
        } else if (c == char.class) {
            c = Character.class;
        } else if (c == byte.class) {
            c = Byte.class;
        } else if (c == short.class) {
            c = Short.class;
        }
        return c;
    }
}
