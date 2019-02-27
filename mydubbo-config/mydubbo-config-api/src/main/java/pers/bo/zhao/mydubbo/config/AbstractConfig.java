package pers.bo.zhao.mydubbo.config;

import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;
import pers.bo.zhao.mydubbo.common.utils.StringUtils;
import pers.bo.zhao.mydubbo.config.support.Parameter;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Bo.Zhao
 * @since 19/2/12
 */
public abstract class AbstractConfig implements Serializable {
    private static final long serialVersionUID = 4267533505537413570L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfig.class);

    private static final int MAX_LENGTH = 200;
    private static final int MAX_PATH_LENGTH = 200;

    private static final Pattern NAME_PATTERN = Pattern.compile("[\\-._0-9a-zA-Z]+");

    private static final Pattern PATTERN_MULTI_NAME = Pattern.compile("[,\\-._0-9a-zA-Z]+");

    private static final Pattern PATTERN_METHOD_NAME = Pattern.compile("[a-zA-Z][0-9a-zA-Z]*");

    private static final Pattern PATTERN_PATH = Pattern.compile("[/\\-$._0-9a-zA-Z]+");

    private static final Pattern PATTERN_NAME_HAS_SYMBOL = Pattern.compile("[:*,/\\-._0-9a-zA-Z]+");

    private static final Pattern PATTERN_KEY = Pattern.compile("[*,\\-._0-9a-zA-Z]+");

    private static final Map<String, String> LEGACY_PROPERTIES = new HashMap<>();

    private static final String[] SUFFIXES = new String[]{"Config", "Bean"};

    static {
        LEGACY_PROPERTIES.put("dubbo.protocol.name", "dubbo.service.protocol");
        LEGACY_PROPERTIES.put("dubbo.protocol.host", "dubbo.service.server.host");
        LEGACY_PROPERTIES.put("dubbo.protocol.port", "dubbo.service.server.port");
        LEGACY_PROPERTIES.put("dubbo.protocol.threads", "dubbo.service.max.thread.pool.size");
        LEGACY_PROPERTIES.put("dubbo.consumer.timeout", "dubbo.service.invoke.timeout");
        LEGACY_PROPERTIES.put("dubbo.consumer.retries", "dubbo.service.max.retry.providers");
        LEGACY_PROPERTIES.put("dubbo.consumer.check", "dubbo.service.allow.no.provider");
        LEGACY_PROPERTIES.put("dubbo.service.url", "dubbo.service.address");
    }


    protected String id;


    protected void appendProperties(AbstractConfig config) {
        if (config == null) {
            return;
        }

        String prefix = "mydubbo." + getTagName(config.getClass()) + ".";

        Method[] methods = config.getClass().getMethods();
        for (Method method : methods) {
            try {
                String name = method.getName();

                // 判断是否为public修饰的set方法，且仅有一个参数，参数类型为基本类型
                if (name.length() > 3 && name.startsWith("set")
                        && method.getParameterTypes().length == 1
                        && isPrimitive(method.getParameterTypes()[0])) {
                    String property = StringUtils.camelToSplitName(
                            name.substring(3, 4) + name.substring(4), ".");

                    String value = null;
                    if (StringUtils.isNotEmpty(config.getId())) {
                        String pn = prefix + config.getId() + "." + property;
                        value = System.getProperty(pn);
                        if (StringUtils.isNotEmpty(value)) {
                            LOGGER.info("Use System Property " + pn + " to config dubbo");
                        }
                    }

                    if (StringUtils.isEmpty(value)) {
                        String pn = prefix + property;
                        value = System.getProperty(pn);
                        if (StringUtils.isNotEmpty(value)) {
                            LOGGER.info("Use System Property " + pn + " to config dubbo");
                        }
                    }

                    if (StringUtils.isEmpty(value)) {
                        Method getter;
                        try {
                            getter = config.getClass().getMethod("get" + name.substring(3));
                        } catch (NoSuchMethodException e1) {
                            try {
                                getter = config.getClass().getMethod("is" + name.substring(3));
                            } catch (NoSuchMethodException e2) {
                                getter = null;
                            }
                        }

                        if (getter != null) {
                            if (getter.invoke(config) == null) {
                                if (config.getId() != null)
                            }

                        }

                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }


    private static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Boolean.class
                || clazz == Byte.class
                || clazz == Short.class
                || clazz == Character.class
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Float.class
                || clazz == Double.class
                || clazz == String.class;
    }

    private static String getTagName(Class<? extends AbstractConfig> configClass) {
        String tag = configClass.getSimpleName();
        for (String suffix : SUFFIXES) {
            if (tag.endsWith(suffix)) {
                tag = tag.substring(0, tag.length() - suffix.length());
                break;
            }
        }
        tag = tag.toLowerCase();
        return tag;
    }


    @Parameter(excluded = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
