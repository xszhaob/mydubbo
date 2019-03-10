package pers.bo.zhao.mydubbo.config;

import pers.bo.zhao.mydubbo.config.support.Parameter;

import java.util.Arrays;

public class ProviderConfig extends AbstractServiceConfig {

    private static final long serialVersionUID = 6913423882496634749L;

    private String name;

    /**
     * 服务主机名，多网卡选择或指定VIP及域名时使用，为空则自动查找本机IP，建议不要配置，让Dubbo自动获取本机IP
     */
    private String host;

    private Integer port;

    private String contextpath;

    private String threadpool;

    private Integer threads;

    private Integer iothreads;

    private Integer queues;

    private Integer accepts;

    private String codes;

    private String charset;

    private Integer payload;

    private Integer buffer;

    private String transporter;

    private String exchanger;

    private String dispatcher;

    private String networker;

    private String server;

    private String client;

    private String telnet;

    private String prompt;

    private String status;

    private Integer wait;

    private Boolean isDefault;


    public void setProtocol(String protocol) {
        this.protocols = Arrays.asList(new ProtocolConfig[] {new ProtocolConfig(protocol)});
    }

    @Parameter(excluded = true)
    public Boolean isDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    @Parameter(excluded = true)
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Parameter(excluded = true)
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Parameter(excluded = true)
    public String getPath() {
        return getContextpath();
    }

    public void setPath(String path) {
        setContextpath(path);
    }


    @Parameter(excluded = true)
    public String getContextpath() {
        return contextpath;
    }

    public void setContextpath(String contextpath) {
        checkPathName("contextpath", contextpath);
        this.contextpath = contextpath;
    }

    public String getThreadpool() {
        return threadpool;
    }

    public void setThreadpool(String threadpool) {
        this.threadpool = threadpool;
    }
}
