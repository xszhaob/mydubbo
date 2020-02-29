package pers.bo.zhao.mydubbo.config;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.extension.ExtensionLoader;
import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;
import pers.bo.zhao.mydubbo.common.utils.CollectionUtils;
import pers.bo.zhao.mydubbo.common.utils.ConfigUtils;
import pers.bo.zhao.mydubbo.common.utils.ReflectUtils;
import pers.bo.zhao.mydubbo.common.utils.StringUtils;
import pers.bo.zhao.mydubbo.config.support.Parameter;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bo.Zhao
 * @since 19/2/12
 */
public abstract class AbstractConfig implements Serializable {
    private static final long serialVersionUID = 4267533505537413570L;

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfig.class);

    private static final int MAX_LENGTH = 200;
    private static final int MAX_PATH_LENGTH = 200;

    private static final Pattern PATTERN_NAME = Pattern.compile("[\\-._0-9a-zA-Z]+");

    private static final Pattern PATTERN_MULTI_NAME = Pattern.compile("[,\\-._0-9a-zA-Z]+");

    private static final Pattern PATTERN_METHOD_NAME = Pattern.compile("[a-zA-Z][0-9a-zA-Z]*");

    private static final Pattern PATTERN_PATH = Pattern.compile("[/\\-$._0-9a-zA-Z]+");

    private static final Pattern PATTERN_NAME_HAS_SYMBOL = Pattern.compile("[:*,/\\-._0-9a-zA-Z]+");

    private static final Pattern PATTERN_KEY = Pattern.compile("[*,\\-._0-9a-zA-Z]+");

    private static final Map<String, String> LEGACY_PROPERTIES = new HashMap<>();

    private static final String[] SUFFIXES = new String[]{"Config", "Bean"};

    static {
        LEGACY_PROPERTIES.put("mydubbo.protocol.name", "mydubbo.service.protocol");
        LEGACY_PROPERTIES.put("mydubbo.protocol.host", "mydubbo.service.server.host");
        LEGACY_PROPERTIES.put("mydubbo.protocol.port", "mydubbo.service.server.port");
        LEGACY_PROPERTIES.put("mydubbo.protocol.threads", "mydubbo.service.max.thread.pool.size");
        LEGACY_PROPERTIES.put("mydubbo.consumer.timeout", "mydubbo.service.invoke.timeout");
        LEGACY_PROPERTIES.put("mydubbo.consumer.retries", "mydubbo.service.max.retry.providers");
        LEGACY_PROPERTIES.put("mydubbo.consumer.check", "mydubbo.service.allow.no.provider");
        LEGACY_PROPERTIES.put("mydubbo.service.url", "mydubbo.service.address");
    }


    protected String id;


    private static String convertLegacyValue(String key, String value) {
        if (value != null && value.length() > 0) {
            if ("mydubbo.service.max.retry.providers".equals(key)) {
                return String.valueOf(Integer.parseInt(value) - 1);
            } else if ("mydubbo.service.allow.no.provider".equals(key)) {
                return String.valueOf(!Boolean.parseBoolean(value));
            }
        }
        return value;
    }

    /**
     * 把properties中配置的值加入到config中
     */
    protected static void appendProperties(AbstractConfig config) {
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
                            LOGGER.info("Use System Property " + pn + " to config mydubbo");
                        }
                    }

                    if (StringUtils.isEmpty(value)) {
                        String pn = prefix + property;
                        value = System.getProperty(pn);
                        if (StringUtils.isNotEmpty(value)) {
                            LOGGER.info("Use System Property " + pn + " to config mydubbo");
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
                                if (StringUtils.isNotEmpty(config.getId())) {
                                    value = ConfigUtils.getProperty(prefix + config.getId() + "." + property);
                                }
                                if (StringUtils.isEmpty(value)) {
                                    value = ConfigUtils.getProperty(prefix + property);
                                }
                                if (StringUtils.isEmpty(value)) {
                                    String legacyKey = LEGACY_PROPERTIES.get(prefix + property);
                                    if (StringUtils.isNotEmpty(legacyKey)) {
                                        value = convertLegacyValue(legacyKey, ConfigUtils.getProperty(legacyKey));
                                    }
                                }
                            }
                        }
                    }
                    if (StringUtils.isNotEmpty(value)) {
                        method.invoke(config, convertPrimitive(method.getParameterTypes()[0], value));
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }


    public static void appendParameters(Map<String, String> parameters, Object config) {
        appendParameters(parameters, config, null);
    }

    /**
     * 把config中的值加入到parameters的map中
     */
    @SuppressWarnings("unchecked")
    public static void appendParameters(Map<String, String> parameters, Object config, String prefix) {
        if (config == null) {
            return;
        }

        Method[] methods = config.getClass().getMethods();
        for (Method method : methods) {
            try {
                String name = method.getName();
                if (ReflectUtils.isGetMethod(name)
                        && !"getClass".equals(name)
                        && Modifier.isPublic(method.getModifiers())
                        // 没有入参
                        && method.getParameterTypes().length == 0
                        // 返回数据是基本类型（包括包装类）
                        && isPrimitive(method.getReturnType())) {
                    Parameter parameter = method.getAnnotation(Parameter.class);
                    if (method.getReturnType() == Object.class || (parameter != null && parameter.excluded())) {
                        continue;
                    }
                    String key;
                    if (parameter != null && parameter.key().length() > 0) {
                        key = parameter.key();
                    } else {
                        int i = name.startsWith("get") ? 3 : 2;
                        key = StringUtils.camelToSplitName(
                                name.substring(i, i + 1).toLowerCase() + name.substring(i + 1), ".");
                    }
                    Object value = method.invoke(config);
                    String str = String.valueOf(value);
                    if (value != null && str.length() > 0) {
                        if (parameter != null && parameter.escaped()) {
                            str = URL.encode(str);
                        }
                        if (parameter != null && parameter.append()) {
                            String pre = parameters.get(Constants.DEFAULT_KEY + "." + key);
                            if (StringUtils.isNotEmpty(pre)) {
                                str = pre + "," + str;
                            }
                            pre = parameters.get(key);
                            if (StringUtils.isNotEmpty(pre)) {
                                str = pre + "," + str;
                            }
                        }
                        if (StringUtils.isNotEmpty(prefix)) {
                            key = prefix + "." + key;
                        }
                        parameters.put(key, str);
                    } else if (parameter != null && parameter.required()) {
                        throw new IllegalStateException(config.getClass().getSimpleName() + "." + key + " == null");
                    }
                } else if ("getParameters".equals(name)
                        && Modifier.isPublic(method.getModifiers())
                        && method.getParameterTypes().length == 0
                        && method.getReturnType() == Map.class) {
                    Map<String, String> map = (Map<String, String>) method.invoke(config);
                    if (CollectionUtils.isNotEmpty(map)) {
                        String pre = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            parameters.put(pre + entry.getKey().replace("-", "."), entry.getValue());
                        }
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    protected static void appendAttributes(Map<Object, Object> parameters, Object config) {
        appendAttributes(parameters, config, null);
    }

    protected static void appendAttributes(Map<Object, Object> parameters, Object config, String prefix) {
        if (config == null) {
            return;
        }

        Method[] methods = config.getClass().getMethods();
        for (Method method : methods) {
            try {
                String name = method.getName();
                if (ReflectUtils.isGetMethod(name)
                        && !"getClass".equals(name)
                        && Modifier.isPublic(method.getModifiers())
                        && method.getParameterTypes().length == 0
                        && isPrimitive(method.getReturnType())) {
                    Parameter parameter = method.getAnnotation(Parameter.class);
                    if (parameter == null || !parameter.attribute()) {
                        continue;
                    }
                    String key;
                    if (parameter.key().length() > 0) {
                        key = parameter.key();
                    } else {
                        int i = name.startsWith("get") ? 3 : 2;
                        key = StringUtils.camelToSplitName(
                                name.substring(i, i + 1).toLowerCase() + name.substring(i + 1), ".");
                    }

                    Object value = method.invoke(config);
                    if (value != null) {
                        if (StringUtils.isNotEmpty(prefix)) {
                            key = prefix + "." + key;
                        }
                        parameters.put(key, value);
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
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

    private static Object convertPrimitive(Class<?> type, String value) {
        if (type == char.class || type == Character.class) {
            return value.length() > 0 ? value.charAt(0) : "\0";
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.valueOf(value);
        } else if (type == byte.class || type == Byte.class) {
            return Byte.valueOf(value);
        } else if (type == short.class || type == Short.class) {
            return Short.valueOf(value);
        } else if (type == int.class || type == Integer.class) {
            return Integer.valueOf(value);
        } else if (type == long.class || type == Long.class) {
            return Long.valueOf(value);
        } else if (type == float.class || type == Float.class) {
            return Float.valueOf(value);
        } else if (type == double.class || type == Double.class) {
            return Double.valueOf(value);
        }
        return value;
    }

    protected static void checkExtension(Class<?> type, String property, String value) {
        checkName(property, value);

        if (StringUtils.isNotEmpty(value) && !ExtensionLoader.getExtensionLoader(type).hasExtension(value)) {
            throw new IllegalStateException("No such extension " + value + " for " + property + "/" + type.getName());
        }
    }

    protected static void checkMultiExtension(Class<?> type, String property, String value) {
        checkMultiName(property, value);
        if (StringUtils.isNotEmpty(value)) {
            String[] values = value.split("[\\s*[,]+]\\s*");
            for (String name : values) {
                if (name.startsWith(Constants.REMOVE_VALUE_PREFIX)) {
                    name = name.substring(1);
                }
                if (Constants.DEFAULT_KEY.equals(name)) {
                    continue;
                }
                if (!ExtensionLoader.getExtensionLoader(type).hasExtension(name)) {
                    throw new IllegalStateException("No such extension " + name + " for " + property + "/" + type.getName());
                }
            }
        }
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

    protected static void checkLength(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, null);
    }

    protected static void checkPathLength(String property, String value) {
        checkProperty(property, value, MAX_PATH_LENGTH, null);
    }

    protected static void checkName(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, PATTERN_NAME);
    }

    protected static void checkPathName(String property, String value) {
        checkProperty(property, value, MAX_PATH_LENGTH, PATTERN_PATH);
    }

    protected static void checkKey(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, PATTERN_KEY);
    }

    protected static void checkMultiName(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, PATTERN_MULTI_NAME);
    }

    protected static void checkNameHasSymbol(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, PATTERN_NAME_HAS_SYMBOL);
    }

    protected static void checkMethodName(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, PATTERN_METHOD_NAME);
    }

    protected static void checkParameterName(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return;
        }
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            checkNameHasSymbol(entry.getKey(), entry.getValue());
        }
    }


    protected static void checkParameters(Map<String, String> parameters) {
        if (CollectionUtils.isEmpty(parameters)) {
            return;
        }
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            checkNameHasSymbol(entry.getKey(), entry.getValue());
        }
    }

    protected static void checkProperty(String property, String value, int maxLength, Pattern pattern) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        if (value.length() > maxLength) {
            throw new IllegalStateException("Invalid " + property + "=\"" + value + "\"" + " is longer than " + maxLength);
        }
        if (pattern != null) {
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                throw new IllegalStateException("Invalid " + property + "=\"" + value + "\"" +
                        " contains illegal character, only digit, letter, '-', '_' or '.' is legal.");
            }
        }
    }


    @Parameter(excluded = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    protected void appendAnnotation(Class<?> annotationClass, Object annotation) {
        Method[] methods = annotationClass.getMethods();

        for (Method method : methods) {
            try {
                if (method.getDeclaringClass() != Object.class
                        && method.getReturnType() != void.class
                        && method.getParameterTypes().length == 0
                        && Modifier.isPublic(method.getModifiers())
                        && !Modifier.isStatic(method.getModifiers())) {
                    String property = method.getName();
                    if ("interfaceClass".equals(property) || "interfaceName".equals(property)) {
                        property = "interface";
                    }
                    String setter = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
                    Object value = method.invoke(annotation);
                    if (value != null && !value.equals(method.getDefaultValue())) {
                        Class<?> parameterType = ReflectUtils.getBoxedClass(method.getReturnType());
                        if ("filter".equals(property) || "listener".equals(property)) {
                            parameterType = String.class;
                            value = String.join(",", (String[]) value);
                        } else if ("parameters".equals(property)) {
                            parameterType = Map.class;
                            value = CollectionUtils.toStringMap((String[]) value);
                        }
                        try {
                            Method invokeMethod = getClass().getMethod(setter, parameterType);
                            invokeMethod.invoke(this, value);
                        } catch (NoSuchMethodException ignore) {
                        }
                    }
                }
            } catch (Throwable t) {
                LOGGER.error(t.getMessage(), t);
            }
        }
    }

    @Override
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<mydubbo:");
            sb.append(getTagName(getClass()));
            Method[] methods = getClass().getMethods();
            for (Method method : methods) {
                try {
                    String name = method.getName();
                    if (ReflectUtils.isGetMethod(name)
                            && !"get".equals(name) && !"is".equals(name)
                            && Modifier.isPublic(method.getModifiers())
                            && !Modifier.isStatic(method.getModifiers())
                            && method.getParameterTypes().length == 0
                            && isPrimitive(method.getReturnType())) {
                        int i = name.startsWith("get") ? 3 : 2;
                        String key = name.substring(i, i + 1).toLowerCase() + name.substring(i + 1);
                        Object value = method.invoke(this);
                        if (value != null) {
                            sb.append(" ");
                            sb.append(key);
                            sb.append("=\"");
                            sb.append(value);
                            sb.append("\"");
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
            sb.append(" />");
            return sb.toString();
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
            return super.toString();
        }
    }
}
