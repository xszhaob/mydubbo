package pers.bo.zhao.mydubbo.common.utils;

public class MethodUtils {

    public static boolean isGetMethod(String methodName) {
        return methodName.startsWith("get") || methodName.startsWith("is");
    }
}
