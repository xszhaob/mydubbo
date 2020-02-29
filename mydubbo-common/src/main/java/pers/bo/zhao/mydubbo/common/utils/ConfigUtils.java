package pers.bo.zhao.mydubbo.common.utils;


import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bo.Zhao
 * @since 19/2/27
 */
public class ConfigUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtils.class);

    /**
     * $：匹配输入字符串的结尾位置。如果设置了 RegExp 对象的 Multiline 属性，则 $ 也匹配 '\n' 或 '\r'。要匹配 $ 字符本身，请使用 \$
     * \s：匹配任何空白字符，包括空格、制表符、换页符等等。等价于 [ \f\n\r\t\v]。注意 Unicode 正则表达式会匹配全角空格符
     * {：标记限定符表达式的开始。要匹配 {，请使用 \{。
     * *：匹配前面的子表达式零次或多次。要匹配 * 字符，请使用 \*。
     * ?：匹配前面的子表达式零次或一次，或指明一个非贪婪限定符。要匹配 ? 字符，请使用 \?。
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\s*\\{?\\s*([._0-9a-zA-Z]+)\\s*}?");

    private static volatile Properties PROPERTIES;

    private static int PID = -1;

    private ConfigUtils() {
    }


    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static boolean isEmpty(String value) {
        return value == null || value.length() == 0
                || "false".equalsIgnoreCase(value)
                || "0".equalsIgnoreCase(value)
                || "null".equalsIgnoreCase(value)
                || "N/A".equalsIgnoreCase(value);
    }

    public static String getProperty(String key) {
        return getProperty(key, null);
    }


    @SuppressWarnings("unchecked")
    public static String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (StringUtils.isNotEmpty(value)) {
            return value;
        }
        Properties properties = getProperties();
        return replaceProperty(properties.getProperty(key, defaultValue), (Map) properties);
    }

    public static Properties getProperties() {
        if (PROPERTIES == null) {
            synchronized (ConfigUtils.class) {
                if (PROPERTIES == null) {
                    String path = System.getProperty(Constants.DUBBO_PROPERTIES_KEY);
                    if (StringUtils.isEmpty(path)) {
                        path = System.getenv(Constants.DUBBO_PROPERTIES_KEY);
                    }
                    if (StringUtils.isEmpty(path)) {
                        path = Constants.DUBBO_PROPERTIES_KEY;
                    }
                    PROPERTIES = loadProperties(path, false, true);
                }
            }
        }
        return PROPERTIES;
    }

    public static void setProperties(Properties properties) {
        PROPERTIES = properties;
    }

    public static String replaceProperty(String expression, Map<String, String> params) {
        if (StringUtils.isEmpty(expression) || expression.indexOf('$') < 0) {
            return expression;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(expression);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = System.getProperty(key);
            if (value == null && params != null) {
                value = params.get(key);
            }
            if (value == null) {
                value = "";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static Properties loadProperties(String fileName, boolean allowMultiFile, boolean optional) {
        Properties properties = new Properties();
        if (fileName.startsWith("/")) {
            try {
                try (FileInputStream input = new FileInputStream(fileName)) {
                    properties.load(input);
                }
            } catch (Throwable e) {
                LOGGER.warn("Failed to load " + fileName + " from " + fileName + "(ignore this file): " + e.getMessage(), e);
            }
        }

        List<URL> list = new ArrayList<>();
        try {
            Enumeration<URL> resources = ClassHelper.getClassLoader().getResources(fileName);
            while (resources.hasMoreElements()) {
                list.add(resources.nextElement());
            }
        } catch (Throwable t) {
            LOGGER.warn("Failed to load " + fileName + " file: " + t.getMessage(), t);
        }

        if (list.isEmpty()) {
            if (!optional) {
                LOGGER.warn("No " + fileName + " found on the class path.");
            }
            return properties;
        }

        if (!allowMultiFile) {
            if (list.size() > 1) {
                String errMsg = String.format("only 1 %s file is expected, but %d dubbo.properties files found on class path: %s",
                        fileName, list.size(), list.toString());
                LOGGER.warn(errMsg);
            }

            try {
                properties.load(ClassHelper.getClassLoader().getResourceAsStream(fileName));
                return properties;
            } catch (Throwable e) {
                LOGGER.warn("Failed to load " + fileName + " file from " + fileName + "(ignore this file): " + e.getMessage(), e);
            }
        }

        LOGGER.info("load " + fileName + " properties file from " + list);

        for (URL url : list) {
            try {
                Properties prop = new Properties();
                try (InputStream input = url.openStream()) {
                    prop.load(input);
                    properties.putAll(prop);
                }
            } catch (Throwable e) {
                LOGGER.warn("Fail to load " + fileName + " file from " + url + "(ignore this file): " + e.getMessage(), e);
            }
        }
        return properties;
    }

    public static int getPid() {
        if (PID < 0) {
            try {
                RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
                String name = runtime.getName();
                PID = Integer.parseInt(name.substring(0, name.indexOf('@')));
            } catch (Throwable t) {
                PID = 0;
            }
        }
        return PID;
    }
}
