package pers.bo.zhao.mydubbo.common.utils;


import pers.bo.zhao.mydubbo.common.Constants;

import java.util.Properties;

/**
 * @author Bo.Zhao
 * @since 19/2/27
 */
public class ConfigUtils {

    private static volatile Properties PROPERTIES;

    public static String getProperty(String key) {
        return getProperty(key, null);
    }


    public static String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (StringUtils.isNotEmpty(value)) {
            return value;
        }

        return null;
    }

    public static Properties getProperties() {
        if (PROPERTIES == null) {
            synchronized (ConfigUtils.class) {
                if (PROPERTIES == null) {
                    String path = System.getProperty(Constants.DUBBO_PROPERTIS_KEY);
                    if (StringUtils.isEmpty(path)) {
                        path = System.getenv(Constants.DUBBO_PROPERTIS_KEY);
                    }
                    if (StringUtils.isEmpty(path)) {
                        path = Constants.DUBBO_PROPERTIS_KEY;
                    }
                    loadProperties(path, false, true);
                }
            }
        }
        return PROPERTIES;
    }

    private static void loadProperties(String path, boolean allowMultiFile, boolean optional) {
        return;
    }
}
