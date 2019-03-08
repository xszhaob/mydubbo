package pers.bo.zhao.mydubbo.common;

import java.util.regex.Pattern;

/**
 * @author Bo.Zhao
 * @since 19/2/27
 */
public class Constants {

    public static final String DUBBO_PROPERTIS_KEY = "dubbo.properties.file";

    public static final String DEFAULT_KEY = "default";

    public static final String RETURN_PREFIX = "return ";

    public static final Pattern COMMA_SPLIT_PATTERN = Pattern
            .compile("\\s*[,]+\\s*");

    public static final String REFERENCE_FILTER_KEY = "reference.filter";

    public static final String INVOKER_LISTENER_KEY = "invoker.listener";

}
