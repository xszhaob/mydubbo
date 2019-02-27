package pers.bo.zhao.mydubbo.common.utils;

import java.util.Collection;
import java.util.Map;

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

    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Collection c) {
        return c != null && c.size() > 0;
    }

    public static boolean isNotEmpty(Map map) {
        return map != null && map.size() > 0;
    }
}
