package pers.bo.zhao.mydubbo.common.extension;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.compiler.Compiler;
import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;
import pers.bo.zhao.mydubbo.common.utils.CollectionUtils;
import pers.bo.zhao.mydubbo.common.utils.ConcurrentHashSet;
import pers.bo.zhao.mydubbo.common.utils.Holder;
import pers.bo.zhao.mydubbo.common.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * @author Bo.Zhao
 * @since 19/1/26
 */
public class ExtensionLoader<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionLoader.class);

    /**
     * Class和对应ExtensionLoader的映射
     */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    /**
     * Class和对应的扩展类实例
     */
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    /**
     * 扩展点的自适应类
     */
    private volatile Class<?> cachedAdaptiveClass = null;
    /**
     * 扩展点的自适应实例
     */
    private final Holder<Object> cachedAdaptiveInstance = new Holder<>();
    /**
     * 创建扩展点失败的Error
     */
    private volatile Throwable createAdaptiveInstanceError = null;
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private Set<Class<?>> cachedWrapperClasses;

    private final Map<String, Object> cachedActivates = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<>();

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    private static final String SERVICES_DIRECTORY = "META-INF/services/";
    private static final String DUBBO_DIRECTORY = "META-INF/dubbo/";
    private static final String DUBBO_INTERNAL_DIRECTORY = DUBBO_DIRECTORY + "internal/";


    private final Class<?> type;

    private final ExtensionFactory extensionFactory;

    private String cachedDefaultName;

    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
        // 如果type是ExtensionFactory.class，则没有extensionFactory
        if (type == ExtensionFactory.class) {
            extensionFactory = null;
        } else {
            extensionFactory = ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {
            String errMsg = String.format("Extension type (%s) is not interface!", type);
            throw new IllegalArgumentException(errMsg);
        }
        if (!withExtensionAnnotation(type)) {
            String errMsg = String.format("Extension type (%s) is not extension, because without @%s Annotation!",
                    type, SPI.class.getSimpleName());
            throw new IllegalArgumentException(errMsg);
        }
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<>(type));
            extensionLoader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    public String getExtensionName(T instance) {
        return getExtensionName(instance.getClass());
    }

    private String getExtensionName(Class<?> clazz) {
        // 先加载class
        getExtensionClasses();
        return cachedNames.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public T getAdaptiveExtension() {
        Object instance = cachedAdaptiveInstance.get();
        if (instance != null) {
            return (T) instance;
        }
        if (createAdaptiveInstanceError != null) {
            throw new IllegalStateException("fail to create instance: "
                    + createAdaptiveInstanceError.toString(), createAdaptiveInstanceError);
        }
        synchronized (cachedAdaptiveInstance) {
            instance = cachedAdaptiveInstance.get();
            if (instance == null) {
                instance = createAdaptiveExtension();
                cachedAdaptiveInstance.set(instance);
            }
        }
        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    public T getExtension(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Extension name == null");
        }
        if ("true".equals(name)) {
            return getDefaultExtension();
        }

        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    private Object createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw findException(name);
        }
        try {
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }

            // 注入实例
            injectExtension(instance);

            Set<Class<?>> wrapperClasses = this.cachedWrapperClasses;
            if (CollectionUtils.isNotEmpty(wrapperClasses)) {
                for (Class<?> wrapperClass : wrapperClasses) {
                    injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
                }
            }

            return instance;
        } catch (Throwable e) {
            throw new IllegalStateException("Extension instance(name: " + name + ",class: " + type.getName() + ") could not be instantiated: " + e.getMessage(), e);
        }
    }

    private IllegalStateException findException(String name) {
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
                return entry.getValue();
            }
        }

        StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name);

        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (i == 1) {
                buf.append(", possible causes: ");
            }
            buf.append("\r\n(").append(i++).append(") ")
                    .append(entry.getKey())
                    .append(":\r\n")
                    .append(StringUtils.toString(entry.getValue()));
        }
        return new IllegalStateException(buf.toString());
    }

    public T getDefaultExtension() {
        getExtensionClasses();

        if (StringUtils.isEmpty(cachedDefaultName) || "true".equals(cachedDefaultName)) {
            return null;
        }

        return getExtension(cachedDefaultName);
    }

    public boolean hasExtension(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Extension name == null");
        }

        try {
            getExtensionClass(name);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Class<?> getExtensionClass(String name) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type = null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Extension name = null");
        }
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new IllegalStateException("No such extension \"" + name + "\" for " + type.getName() + "!");
        }
        return clazz;

    }

    @SuppressWarnings("unchecked")
    private T createAdaptiveExtension() {
        try {
            return injectExtension((T) getAdaptiveExtensionClass().newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Can not create adaptive extension " + type.getSimpleName() + ", case: " + e.getMessage(), e);
        }
    }


    private T injectExtension(T instance) {
        try {
            if (extensionFactory != null) {
                for (Method method : instance.getClass().getMethods()) {
                    if (method.getName().startsWith("set")
                            && method.getParameterTypes().length == 1
                            && Modifier.isPublic(method.getModifiers())) {
                        Class<?> pt = method.getParameterTypes()[0];
                        try {
                            String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
                            Object obj = extensionFactory.getExtension(pt, property);
                            if (obj != null) {
                                method.invoke(instance, obj);
                            }
                        } catch (Exception e) {
                            LOGGER.error("fail to inject via method " + method.getName() + " of instance " + type.getName() + ": " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return instance;
    }


    private Class<?> getAdaptiveExtensionClass() {
        getExtensionClasses();
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass;
        }
        return cachedAdaptiveClass = createAdaptiveExtensionClass();
    }

    private Class<?> createAdaptiveExtensionClass() {
        String code = createAdaptiveExtensionClassCode();
        ClassLoader classLoader = findClassLoader();
        Compiler compiler = ExtensionLoader.getExtensionLoader(Compiler.class).getAdaptiveExtension();
        return compiler.compile(code, classLoader);
    }

    private ClassLoader findClassLoader() {
        return ExtensionLoader.class.getClassLoader();
    }

    private String createAdaptiveExtensionClassCode() {
        StringBuilder codeBuilder = new StringBuilder();
        Method[] methods = type.getMethods();
        boolean hasAdaptiveAnnotation = false;
        for (Method m : methods) {
            if (m.isAnnotationPresent(Adaptive.class)) {
                hasAdaptiveAnnotation = true;
                break;
            }
        }
        // no need to generate adaptive class since there's no adaptive method found.
        if (!hasAdaptiveAnnotation)
            throw new IllegalStateException("No adaptive method on extension " + type.getName() + ", refuse to create the adaptive class!");

        codeBuilder.append("package ").append(type.getPackage().getName()).append(";");
        codeBuilder.append("\nimport ").append(ExtensionLoader.class.getName()).append(";");
        codeBuilder.append("\npublic class ").append(type.getSimpleName()).append("$Adaptive").append(" implements ").append(type.getCanonicalName()).append(" {");

        for (Method method : methods) {
            Class<?> rt = method.getReturnType();
            Class<?>[] pts = method.getParameterTypes();
            Class<?>[] ets = method.getExceptionTypes();

            Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
            StringBuilder code = new StringBuilder(512);
            if (adaptiveAnnotation == null) {
                code.append("throw new UnsupportedOperationException(\"method ")
                        .append(method.toString()).append(" of interface ")
                        .append(type.getName()).append(" is not adaptive method!\");");
            } else {
                int urlTypeIndex = -1;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].equals(URL.class)) {
                        urlTypeIndex = i;
                        break;
                    }
                }
                // found parameter in URL type
                if (urlTypeIndex != -1) {
                    // Null Point check
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"url == null\");",
                            urlTypeIndex);
                    code.append(s);

                    s = String.format("\n%s url = arg%d;", URL.class.getName(), urlTypeIndex);
                    code.append(s);
                }
                // did not find parameter in URL type
                else {
                    String attribMethod = null;

                    // find URL getter method
                    LBL_PTS:
                    for (int i = 0; i < pts.length; ++i) {
                        Method[] ms = pts[i].getMethods();
                        for (Method m : ms) {
                            String name = m.getName();
                            if ((name.startsWith("get") || name.length() > 3)
                                    && Modifier.isPublic(m.getModifiers())
                                    && !Modifier.isStatic(m.getModifiers())
                                    && m.getParameterTypes().length == 0
                                    && m.getReturnType() == URL.class) {
                                urlTypeIndex = i;
                                attribMethod = name;
                                break LBL_PTS;
                            }
                        }
                    }
                    if (attribMethod == null) {
                        throw new IllegalStateException("fail to create adaptive class for interface " + type.getName()
                                + ": not found url parameter or url attribute in parameters of method " + method.getName());
                    }

                    // Null point check
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");",
                            urlTypeIndex, pts[urlTypeIndex].getName());
                    code.append(s);
                    s = String.format("\nif (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");",
                            urlTypeIndex, attribMethod, pts[urlTypeIndex].getName(), attribMethod);
                    code.append(s);

                    s = String.format("%s url = arg%d.%s();", URL.class.getName(), urlTypeIndex, attribMethod);
                    code.append(s);
                }

                String[] value = adaptiveAnnotation.value();
                // value is not set, use the value generated from class name as the key
                if (value.length == 0) {
                    char[] charArray = type.getSimpleName().toCharArray();
                    StringBuilder sb = new StringBuilder(128);
                    for (int i = 0; i < charArray.length; i++) {
                        if (Character.isUpperCase(charArray[i])) {
                            if (i != 0) {
                                sb.append(".");
                            }
                            sb.append(Character.toLowerCase(charArray[i]));
                        } else {
                            sb.append(charArray[i]);
                        }
                    }
                    value = new String[]{sb.toString()};
                }

                boolean hasInvocation = false;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].getName().equals("org.apache.dubbo.rpc.Invocation")) {
                        // Null Point check
                        String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"invocation == null\");", i);
                        code.append(s);
                        s = String.format("\nString methodName = arg%d.getMethodName();", i);
                        code.append(s);
                        hasInvocation = true;
                        break;
                    }
                }

                String defaultExtName = cachedDefaultName;
                String getNameCode = null;
                for (int i = value.length - 1; i >= 0; --i) {
                    if (i == value.length - 1) {
                        if (null != defaultExtName) {
                            if (!"protocol".equals(value[i]))
                                if (hasInvocation)
                                    getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                                else
                                    getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], defaultExtName);
                            else
                                getNameCode = String.format("( url.getProtocol() == null ? \"%s\" : url.getProtocol() )", defaultExtName);
                        } else {
                            if (!"protocol".equals(value[i]))
                                if (hasInvocation)
                                    getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                                else
                                    getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                            else
                                getNameCode = "url.getProtocol()";
                        }
                    } else {
                        if (!"protocol".equals(value[i]))
                            if (hasInvocation)
                                getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                            else
                                getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i], getNameCode);
                        else
                            getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()", getNameCode);
                    }
                }
                code.append("\nString extName = ").append(getNameCode).append(";");
                // check extName == null?
                String s = String.format("\nif(extName == null) " +
                                "throw new IllegalStateException(\"Fail to get extension(%s) name from url(\" + url.toString() + \") use keys(%s)\");",
                        type.getName(), Arrays.toString(value));
                code.append(s);

                s = String.format("\n%s extension = (%<s)%s.getExtensionLoader(%s.class).getExtension(extName);",
                        type.getName(), ExtensionLoader.class.getSimpleName(), type.getName());
                code.append(s);

                // return statement
                if (!rt.equals(void.class)) {
                    code.append("\nreturn ");
                }

                s = String.format("extension.%s(", method.getName());
                code.append(s);
                for (int i = 0; i < pts.length; i++) {
                    if (i != 0)
                        code.append(", ");
                    code.append("arg").append(i);
                }
                code.append(");");
            }

            codeBuilder.append("\npublic ").append(rt.getCanonicalName()).append(" ").append(method.getName()).append("(");
            for (int i = 0; i < pts.length; i++) {
                if (i > 0) {
                    codeBuilder.append(", ");
                }
                codeBuilder.append(pts[i].getCanonicalName());
                codeBuilder.append(" ");
                codeBuilder.append("arg").append(i);
            }
            codeBuilder.append(")");
            if (ets.length > 0) {
                codeBuilder.append(" throws ");
                for (int i = 0; i < ets.length; i++) {
                    if (i > 0) {
                        codeBuilder.append(", ");
                    }
                    codeBuilder.append(ets[i].getCanonicalName());
                }
            }
            codeBuilder.append(" {");
            codeBuilder.append(code.toString());
            codeBuilder.append("\n}");
        }
        codeBuilder.append("\n}");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(codeBuilder.toString());
        }
        return codeBuilder.toString();
    }

    private String adaptiveMethodBody(Class<?>[] parameterTypes, Adaptive adaptiveAnnotation, Class<?> returnType) {
        StringBuilder code = new StringBuilder();
        int urlTypeIndex = -1;
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(pers.bo.zhao.mydubbo.common.URL.class)) {
                urlTypeIndex = i;
                break;
            }
        }
        if (urlTypeIndex > 0) {
            String s = String.format("if (arg%d == null) {\n " +
                    "throw new IllegalArgumentException(\"url = null\"); " +
                    "\n}", urlTypeIndex);
            code.append(s);

            s = String.format("%s url = arg%d;", pers.bo.zhao.mydubbo.common.URL.class.getName(), urlTypeIndex);
            code.append(s);
        } else {
            // 暂不实现
        }
        String[] value = adaptiveAnnotation.value();
        if (value.length == 0) {
            char[] chars = type.getSimpleName().toCharArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < chars.length; i++) {
                if (Character.isUpperCase(chars[i])) {
                    if (i != 0) {
                        sb.append(".");
                    }
                    sb.append(Character.toLowerCase(chars[i]));
                } else {
                    sb.append(chars[i]);
                }
            }
            value = new String[]{sb.toString()};
        }
        boolean hasInvocation = false;
        for (int i = 0; i < parameterTypes.length; i++) {
            if ("pers.bo.zhao.mydubbo.rpc.Invocation".equals(parameterTypes[i].getName())) {
                String s = String.format("\nif (arg%d == null) {" +
                        "\n throw new IllegalArgumentException(\"invacation == null\");" +
                        "\n}", i);
                code.append(s);
                s = String.format("\nString methodName = arg%d.getMethodName();", i);
                code.append(s);
                hasInvocation = true;
                break;
            }
        }

        String defaultExtName = cachedDefaultName;
        String getNameCode = null;
        for (int i = value.length - 1; i >= 0; i--) {
            if (i == value.length - 1) {
                if (defaultExtName != null) {
                    if (!"protocol".equals(value[i])) {
                        if (hasInvocation) {
                            getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                        } else {
                            getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], defaultExtName);
                        }
                    } else {
                        getNameCode = String.format("url.getProtocol() == null ? %s : url.getProtocol()", defaultExtName);
                    }
                } else {
                    if (!"protocol".equals(value[i])) {
                        if (hasInvocation) {
                            getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                        } else {
                            getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                        }
                    } else {
                        getNameCode = "url.getProtocol() == null";
                    }
                }

            } else {
                if (!"protocol".equals(value[i])) {
                    if (hasInvocation) {
                        getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                    } else {
                        getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], getNameCode);
                    }
                } else {
                    getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()", getNameCode);
                }
            }
        }
        code.append("\nString extName = ").append(getNameCode).append(";");
        // check npe
        String s = String.format("if (extName == null) {\n throw new IllegalArgumentException(\"Fail to get extension(%s) from url(\" + url.toString() + \") use keys(%s);", type.getName(), Arrays.toString(value));
        code.append(s);

        code.append(String.format("\n%s extension = null;\n try { \nextension = (%s)%s.getExtensionLoader(%s.class).getExtension(extName); \n} catch(Exception e) {\n", type.getName(), type.getName(), ExtensionLoader.class.getSimpleName(), type.getName()));
        code.append(String.format("if (count.incrementAndGet() == 1) {\nlogger.warn(\"Failed to find extension name \" + extName + \" for type %s, will use default extension %s instead.\", e);\n}\n", type.getName(), defaultExtName));
        code.append(String.format("extension = (%s)%s.getExtensionLoader(%s.class).getExtension(\"%s\");\n}", type.getName(), ExtensionLoader.class.getSimpleName(), type.getName(), defaultExtName));

        if (!returnType.equals(void.class)) {
            code.append("\nreturn ");
        }
        code.append("extension.%s(");
        for (int j = 0; j < parameterTypes.length; j++) {
            if (j != 0) {
                code.append(", ");
            }
            code.append("arg").append(j);
        }
        code.append(");");

        return code.toString();
    }

    private String unsupported(Method method, Class<?> type) {
        return "throw new UnsupportedOperationException(\"method " + method.toString() + " of interface " + type.getName() + " is not adaptive method!\"";
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }

        return classes;
    }

    public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> map = getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<>(map.keySet()));
    }

    /**
     * Return default extension name, return <code>null</code> if not configured.
     */
    public String getDefaultExtensionName() {
        getExtensionClasses();
        return cachedDefaultName;
    }

    private Map<String, Class<?>> loadExtensionClasses() {
        SPI spiAnnotation = type.getAnnotation(SPI.class);
        if (spiAnnotation != null) {
            String defaultValue = spiAnnotation.value();
            if ((defaultValue = defaultValue.trim()).length() > 0) {
                String[] split = NAME_SEPARATOR.split(defaultValue);
                cachedDefaultName = split[0];
            }
        }
        Map<String, Class<?>> extensionClasses = new HashMap<>();
        loadInDirectory(extensionClasses, SERVICES_DIRECTORY, type.getName());
        loadInDirectory(extensionClasses, DUBBO_DIRECTORY, type.getName());
        loadInDirectory(extensionClasses, DUBBO_INTERNAL_DIRECTORY, type.getName());
        return extensionClasses;
    }


    private void loadInDirectory(Map<String, Class<?>> extensionClasses, String dir, String typeName) {
        String fileName = dir + typeName;
        try {
            Enumeration<java.net.URL> urls;
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }

            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL url = urls.nextElement();
                    loadResource(extensionClasses, classLoader, url);
                }
            }
        } catch (Throwable e) {
            LOGGER.error("Exception when load extension class(interface: " + typeName + ",description file: " + fileName + ").", e);
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, java.net.URL url) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String name = null;
                    int i = line.indexOf("=");
                    if (i > 0) {
                        name = line.substring(0, i).trim();
                        line = line.substring(i + 1).trim();
                    }
                    if (line.length() > 0) {
                        loadClass(extensionClasses, url, Class.forName(line, true, classLoader), name);
                    }

                } catch (Throwable t) {
                    IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ",class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                    exceptions.put(line, e);
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Exception when load extension class(interface: " +
                    type + ", class file: " + url + ") in " + url, t);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("Exception when load extension class(interface: " +
                            type + ", class file: " + url + ") in " + url, e);
                }
            }
        }
    }

    private void loadClass(Map<String, Class<?>> extensionClasses, java.net.URL url, Class<?> clazz, String name) {
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Error when load extension class(interface:" +
                    type + ",class line:" + clazz.getName() + "),class "
                    + clazz.getName() + " is not subtype of interface.");
        }
        if (clazz.isAnnotationPresent(Adaptive.class)) {
            if (cachedAdaptiveClass == null) {
                cachedAdaptiveClass = clazz;
            } else if (!cachedAdaptiveClass.equals(clazz)) {
                throw new IllegalStateException("More than 1 adaptive class found:" +
                        cachedAdaptiveClass.getName() + ", " + clazz.getName());
            }
        } else if (isWrapperClass(clazz)) {
            Set<Class<?>> wrappers = cachedWrapperClasses;
            if (wrappers == null) {
                cachedWrapperClasses = new ConcurrentHashSet<>();
                wrappers = cachedWrapperClasses;
            }
            wrappers.add(clazz);
        } else {
            String[] names = NAME_SEPARATOR.split(name);
            if (names != null && names.length > 0) {
                Activate activate = clazz.getAnnotation(Activate.class);
                if (activate != null) {
                    cachedActivates.put(names[0], activate);
                }
                for (String n : names) {
                    if (!cachedNames.containsKey(clazz)) {
                        cachedNames.put(clazz, n);
                    }
                    Class<?> c = extensionClasses.get(n);
                    if (c == null) {
                        extensionClasses.put(n, clazz);
                    } else if (c != clazz) {
                        throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + n + " on " + clazz.getName() + " and " + c.getName());
                    }
                }
            }
        }
    }


    private boolean isWrapperClass(Class<?> clazz) {
        try {
            clazz.getConstructor(type);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }


    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

}
