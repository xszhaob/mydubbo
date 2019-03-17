package pers.bo.zhao.mydubbo.common;

import java.util.regex.Pattern;

/**
 * @author Bo.Zhao
 * @since 19/2/27
 */
public class Constants {

    public static final String DUBBO_PROPERTIES_KEY = "dubbo.properties.file";

    public static final String DEFAULT_KEY = "default";
    public static final String DEFAULT_KEY_PREFIX = "default.";

    public static final String RETURN_PREFIX = "return ";

    public static final Pattern COMMA_SPLIT_PATTERN = Pattern
            .compile("\\s*[,]+\\s*");

    public static final String REFERENCE_FILTER_KEY = "reference.filter";

    public static final String SERVICE_FILTER_KEY = "service.filter";

    public static final String EXPORTER_LISTENER_KEY = "exporter.listener";

    public static final String INVOKER_LISTENER_KEY = "invoker.listener";

    public static final String REMOVE_VALUE_PREFIX = "-";

    public static final String THREADPOOL_KEY = "threadpool";


    public static final String THREAD_NAME_KEY = "threadname";

    public static final String DEFAULT_THREAD_NAME = "MyDubbo";

    public static final String THREADS_KEY = "threads";

    public static final int DEFAULT_THREADS = 200;

    public static final String QUEUES_KEY = "queues";

    public static final int DEFAULT_QUEUES = 0;

    public static final String APPLICATION_KEY = "application";

    public static final String DUMP_DIRECTORY = "dump.directory";

    public static final String QOS_ENABLE = "qos.enable";

    public static final String QOS_PORT = "qos.port";

    public static final String ACCEPT_FOREIGN_IP = "qos.accept.foreign.ip";

    public static final String ON_INVOKE_METHOD_KEY = "oninvoke.method";

    public static final String ON_RETURN_METHOD_KEY = "onreturn.method";

    public static final String ON_THROW_METHOD_KEY = "onthrow.method";

    public static final String ON_INVOKE_INSTANCE_KEY = "oninvoke.instance";

    public static final String ON_RETURN_INSTANCE_KEY = "onreturn.instance";

    public static final String ON_THROW_INSTANCE_KEY = "onthrow.instance";

}
