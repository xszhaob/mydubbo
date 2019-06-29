package pers.bo.zhao.mydubbo.common;

import java.util.regex.Pattern;

/**
 * @author Bo.Zhao
 * @since 19/2/27
 */
public class Constants {

    public static final String ANY_VALUE = "*";
    public static final String ANY_HOST_VALUE = "0.0.0.0";
    public static final String MYDUBBO_VERSION_KEY = "mydubbo";
    public static final String MYDUBBO_PROPERTIES_KEY = "mydubbo.properties.file";

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

    public static final String SHUTDOWN_WAIT_KEY = "mydubbo.service.shutdown.wait";

    public static final String CODEC_KEY = "codec";

    public static final String TIMESTAMP_KEY = "timestamp";

    public static final String PID_KEY = "pid";

    public static final String REGISTRY_KEY = "registry";
    public static final String REGISTRY_PROTOCOL = "registry";
    public static final String REGISTER_KEY = "register";
    public static final String SUBSCRIBE_KEY = "subscribe";
    public static final String REGISTRY_FILE_SAVE_SYNC_KEY = "save.file";
    public static final String FILE_KEY = "file";
    public static final String BACKUP_KEY = "backup";
    public static final String INTERFACE_KEY = "interface";
    public static final String EMPTY_PROTOCOL = "empty";
    public static final String CATEGORY_KEY = "category";
    private static final String PROVIDERS_CATEGORY = "providers";
    public static final String DEFAULT_CATEGORY = PROVIDERS_CATEGORY;
    public static final String GROUP_KEY = "group";
    public static final String VERSION_KEY = "version";

    public static final String REGISTRY_RETRY_PERIOD_KEY = "retry.period";
    public static final int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;
    public static final String REGISTRY_RETRY_TIMES_KEY = "retry.period";
    public static final int DEFAULT_REGISTRY_RETRY_TIMES = 3;


    public static final String CHECK_KEY = "check";

    public static final String CONSUMER_PROTOCOL = "consumer";

    public static final String REMOTE_TIMESTAMP_KEY = "remote.timestamp";

    public static final String WEIGHT_KEY = "weight";

    public static final int DEFAULT_WEIGHT = 100;

    public static final String WARMUP_KEY = "warmup";

    public static final int DEFAULT_WARMUP = 10 * 60 * 1000;

}
