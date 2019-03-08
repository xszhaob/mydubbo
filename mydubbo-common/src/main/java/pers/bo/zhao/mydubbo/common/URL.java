package pers.bo.zhao.mydubbo.common;

import pers.bo.zhao.mydubbo.common.utils.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * URL：统一资源定位。
 * 不可变、线程安全的。
 *
 * @author Bo.Zhao
 * @since 19/2/10
 */
public final class URL implements Serializable {

    private static final long serialVersionUID = -1985165475234910535L;

    /**
     * 协议
     */
    private final String protocol;

    private final String userName;

    private final String password;

    private final String host;

    private final int port;

    private final String path;

    private final Map<String, String> parameters;


    // cache
    private volatile transient Map<String, Number> numbers;

    private volatile transient Map<String, URL> urls;

    private volatile transient String ip;

    private volatile transient String full;

    private volatile transient String identity;

    private volatile transient String parameter;

    private volatile transient String string;


    public URL() {
        this.protocol = null;
        this.userName = null;
        this.password = null;
        this.host = null;
        this.port = 0;
        this.path = null;
        this.parameters = null;
    }


    public URL(String protocol, String userName, String password, String host, int port, String path, Map<String, String> parameters) {
        if (StringUtils.isEmpty(userName) && StringUtils.isNotEmpty(password)) {
            throw new IllegalArgumentException("Invalid url, password without username!");
        }
        this.protocol = protocol;
        this.userName = userName;
        this.password = password;
        this.host = host;
        this.port = port < 0 ? 0 : port;
        if (StringUtils.isNotEmpty(path) && path.startsWith("/")) {
            path = path.substring(1);
        }
        this.path = path;
        if (parameters == null) {
            parameters = new HashMap<>(0);
        } else {
            parameters = new HashMap<>(parameters);
        }
        // 不可修改类
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public static String encode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    public static String decode(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
