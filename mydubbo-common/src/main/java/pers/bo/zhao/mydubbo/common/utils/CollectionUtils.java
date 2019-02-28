package pers.bo.zhao.mydubbo.common.utils;

import java.util.Collection;
import java.util.HashMap;
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

    /**
     * TODO 不好理解
     */
    public static Map<String, String> toStringMap(String... pairs) {
        Map<String, String> parameters = new HashMap<String, String>();
        if (pairs.length > 0) {
            if (pairs.length % 2 != 0) {
                throw new IllegalArgumentException("pairs must be even.");
            }
            for (int i = 0; i < pairs.length; i = i + 2) {
                parameters.put(pairs[i], pairs[i + 1]);
            }
        }
        return parameters;
    }
}
