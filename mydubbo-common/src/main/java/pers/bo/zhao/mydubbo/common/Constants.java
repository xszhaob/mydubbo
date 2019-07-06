package pers.bo.zhao.mydubbo.common;

import java.util.regex.Pattern;

/**
 * @author Bo.Zhao
 * @since 19/2/27
 */
public interface Constants {

    String ANY_VALUE = "*";
    String ANY_HOST_VALUE = "0.0.0.0";
    String MYDUBBO_VERSION_KEY = "mydubbo";
    String MYDUBBO_PROPERTIES_KEY = "mydubbo.properties.file";

    String DEFAULT_KEY = "default";
    String DEFAULT_KEY_PREFIX = "default.";

    String RETURN_PREFIX = "return ";

    Pattern COMMA_SPLIT_PATTERN = Pattern
            .compile("\\s*[,]+\\s*");

    String REFERENCE_FILTER_KEY = "reference.filter";

    String SERVICE_FILTER_KEY = "service.filter";

    String EXPORTER_LISTENER_KEY = "exporter.listener";

    String INVOKER_LISTENER_KEY = "invoker.listener";

    String REMOVE_VALUE_PREFIX = "-";

    String THREADPOOL_KEY = "threadpool";


    String THREAD_NAME_KEY = "threadname";

    String DEFAULT_THREAD_NAME = "MyDubbo";

    String THREADS_KEY = "threads";

    int DEFAULT_THREADS = 200;

    String QUEUES_KEY = "queues";

    int DEFAULT_QUEUES = 0;

    String APPLICATION_KEY = "application";

    String DUMP_DIRECTORY = "dump.directory";

    String QOS_ENABLE = "qos.enable";

    String QOS_PORT = "qos.port";

    String ACCEPT_FOREIGN_IP = "qos.accept.foreign.ip";

    String ON_INVOKE_METHOD_KEY = "oninvoke.method";

    String ON_RETURN_METHOD_KEY = "onreturn.method";

    String ON_THROW_METHOD_KEY = "onthrow.method";

    String ON_INVOKE_INSTANCE_KEY = "oninvoke.instance";

    String ON_RETURN_INSTANCE_KEY = "onreturn.instance";

    String ON_THROW_INSTANCE_KEY = "onthrow.instance";

    String SHUTDOWN_WAIT_KEY = "mydubbo.service.shutdown.wait";

    String CODEC_KEY = "codec";

    String TIMESTAMP_KEY = "timestamp";

    String PID_KEY = "pid";

    String REGISTRY_KEY = "registry";
    String REGISTRY_PROTOCOL = "registry";
    String REGISTER_KEY = "register";
    String SUBSCRIBE_KEY = "subscribe";
    String REGISTRY_FILE_SAVE_SYNC_KEY = "save.file";
    String FILE_KEY = "file";
    String BACKUP_KEY = "backup";
    String INTERFACE_KEY = "interface";
    String EMPTY_PROTOCOL = "empty";
    String CATEGORY_KEY = "category";
    String PROVIDERS_CATEGORY = "providers";
    String DEFAULT_CATEGORY = PROVIDERS_CATEGORY;
    String GROUP_KEY = "group";
    String VERSION_KEY = "version";

    String REGISTRY_RETRY_PERIOD_KEY = "retry.period";
    int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;
    String REGISTRY_RETRY_TIMES_KEY = "retry.period";
    int DEFAULT_REGISTRY_RETRY_TIMES = 3;


    String CHECK_KEY = "check";

    String CONSUMER_PROTOCOL = "consumer";

    String REMOTE_TIMESTAMP_KEY = "remote.timestamp";

    String WEIGHT_KEY = "weight";

    int DEFAULT_WEIGHT = 100;

    String WARMUP_KEY = "warmup";

    int DEFAULT_WARMUP = 10 * 60 * 1000;

    String SENT_KEY = "sent";

    String TIMEOUT_KEY = "timeout";

    int DEFAULT_TIMEOUT = 1000;

    String CONNECT_TIMEOUT_KEY = "connect.timeout";

    int DEFAULT_CONNECT_TIMEOUT = 3000;

    String SEND_RECONNECT_KEY = "send.reconnect";

}
