package pers.bo.zhao.mydubbo.common.utils;

import java.util.Collection;

/**
 * @author Bo.Zhao
 * @since 19/2/11
 */
public class CollectionUtils {

    private CollectionUtils() {
    }


    public static boolean isEmpty(Collection c) {
        return c == null || c.isEmpty();
    }

    public static boolean isNotEmpty(Collection c) {
        return c != null && c.size() > 0;
    }
}
