package pers.bo.zhao.mydubbo.common;

import pers.bo.zhao.mydubbo.common.utils.NetUtils;
import pers.bo.zhao.mydubbo.common.utils.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private final String username;

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
        this.username = null;
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
        this.username = userName;
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

    public String getProtocol() {
        return protocol;
    }

    public URL setProtocol(String protocol) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public String getParameter(String key) {
        String value = parameters.get(key);

        if (StringUtils.isEmpty(value)) {
            value = parameters.get(Constants.DEFAULT_KEY_PREFIX + key);
        }
        return value;
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);

        if (StringUtils.isEmpty(value)) {
            value = defaultValue;
        }
        return value;
    }

    public String[] getParameter(String key, String[] defaultValue) {
        String value = getParameter(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return Constants.COMMA_SPLIT_PATTERN.split(value);
    }

    public int getParameter(String key, int defaultValue) {
        Number number = getNumbers().get(key);
        if (number != null) {
            return number.intValue();
        }

        String value = getParameter(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }

        int i = Integer.parseInt(value);
        getNumbers().put(key, i);
        return i;
    }

    public boolean getParameter(String key, boolean defaultValue) {
        String value = getParameter(key);
        if (StringUtils.isNotEmpty(value)) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    public Map<String, Number> getNumbers() {
        // 允许出现并发
        if (numbers == null) {
            numbers = new ConcurrentHashMap<>();
        }
        return numbers;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public URL addParameter(String key, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return this;
        }
        if (value.equals(getParameters().get(key))) {
            return this;
        }
        Map<String, String> map = new HashMap<>(getParameters());
        map.put(key, value);
        // 协议名，用户名，密码，host，端口号，路径，参数
        return new URL(protocol, username, password, host, port, path, map);
    }


    public URL setAddress(String address) {
        int i = address.indexOf(':');
        String host;
        int port = this.port;
        // 有port的情况
        if (i > 0) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
        }
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }


    public List<URL> getBackupUrls() {
        List<URL> urls = new ArrayList<>();
        urls.add(this);

        String[] backups = getParameter(Constants.BACKUP_KEY, new String[0]);
        if (backups != null && backups.length > 0) {
            for (String backup : backups) {
                urls.add(setAddress(backup));
            }
        }
        return urls;
    }

    public String getServiceInterface() {
        return getParameter(Constants.INTERFACE_KEY, path);
    }

    public String toFullString() {
        if (full != null) {
            return full;
        }
        return full = buildString(true, true);
    }

    private String buildString(boolean appendUser, boolean appendParameter, String... parameters) {
        return buildString(appendUser, appendParameter, false, false, parameters);
    }

    private String buildString(boolean appendUser, boolean appendParameter, boolean useIP, boolean useService, String... parameters) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(sb)) {
            sb.append(protocol);
            sb.append("://");
        }
        if (appendUser && StringUtils.isNotEmpty(username)) {
            sb.append(username);
            if (StringUtils.isNotEmpty(password)) {
                sb.append(":")
                        .append(password);
            }
            sb.append("@");
        }
        String host;
        if (useIP) {
            host = getIP();
        } else {
            host = getHost();
        }
        if (StringUtils.isNotEmpty(host)) {
            sb.append(host);
            if (port > 0) {
                sb.append(":")
                        .append(port);
            }
        }
        String path;
        if (useService) {
            path = getServiceKey();
        } else {
            path = getPath();
        }
        if (StringUtils.isNotEmpty(path)) {
            sb.append("/").append(path);
        }
        if (appendParameter) {
            buildParameter(sb, true, parameters);
        }
        return sb.toString();
    }

    private String getPath() {
        return path;
    }

    private String getIP() {
        if (ip == null) {
            return ip = NetUtils.getIpByHost(host);
        }

        return ip;
    }

    private void buildParameter(StringBuilder sb, boolean concat, String[] parameters) {
        if (getParameters() != null && getParameters().size() > 0) {
            List<String> includes = (parameters == null || parameters.length == 0 ? null : Arrays.asList(parameters));
            boolean first = true;
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(getParameters()).entrySet()) {
                if (entry.getKey() != null && entry.getKey().length() > 0
                        && (includes == null || includes.contains(entry.getKey()))) {
                    if (first) {
                        if (concat) {
                            sb.append("?");
                        }
                        first = false;
                    } else {
                        sb.append("&");
                    }
                    sb.append(entry.getKey());
                    sb.append("=");
                    sb.append(entry.getValue() == null ? "" : entry.getValue().trim());
                }
            }
        }
    }


    public String getServiceKey() {
        String inf = getServiceInterface();
        if (inf == null) return null;
        StringBuilder buf = new StringBuilder();
        String group = getParameter(Constants.GROUP_KEY);
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(inf);
        String version = getParameter(Constants.VERSION_KEY);
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }

    public String getHost() {
        return host;
    }

    public URL setHost() {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }


    @Override
    public String toString() {
        if (string != null) {
            return string;
        }
        // 不展示用户名和密码
        return string = buildString(false, true);
    }

    public static URL valueOf(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = null;
        int i = url.indexOf("?"); // seperator between body and parameters
        if (i >= 0) {
            String[] parts = url.substring(i + 1).split("\\&");
            parameters = new HashMap<String, String>();
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            url = url.substring(0, i);
        }
        i = url.indexOf("://");
        if (i >= 0) {
            if (i == 0) throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        } else {
            // case: file:/path/to/file.txt
            i = url.indexOf(":/");
            if (i >= 0) {
                if (i == 0) throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                protocol = url.substring(0, i);
                url = url.substring(i + 1);
            }
        }

        i = url.indexOf("/");
        if (i >= 0) {
            path = url.substring(i + 1);
            url = url.substring(0, i);
        }
        i = url.lastIndexOf("@");
        if (i >= 0) {
            username = url.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            url = url.substring(i + 1);
        }
        i = url.lastIndexOf(":");
        if (i >= 0 && i < url.length() - 1) {
            if (url.lastIndexOf("%") > i) {
                // ipv6 address with scope id
                // e.g. fe80:0:0:0:894:aeec:f37d:23e1%en0
                // see https://howdoesinternetwork.com/2013/ipv6-zone-id
                // ignore
            } else {
                port = Integer.parseInt(url.substring(i + 1));
                url = url.substring(0, i);
            }
        }
        if (url.length() > 0) host = url;
        return new URL(protocol, username, password, host, port, path, parameters);
    }

}
