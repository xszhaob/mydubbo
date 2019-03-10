package pers.bo.zhao.mydubbo.config;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.config.support.Parameter;
import pers.bo.zhao.mydubbo.rpc.ExporterListener;

import java.util.Arrays;
import java.util.List;

/**
 * @author Bo.Zhao
 * @since 19/2/12
 */
public abstract class AbstractServiceConfig extends AbstractInterfaceConfig {

    private static final long serialVersionUID = 1L;

    /**
     * version
     * 服务版本，建议使用两位数字版本，如：1.0，通常在接口不兼容时版本号才需要升级
     */
    protected String version;

    /**
     * group
     * 服务分组，当一个接口有多个实现，可以用分组区分
     */
    protected String group;

    /**
     * 服务是否过时，如果设为true，消费方引用时将打印服务过时警告error日志
     */
    protected Boolean deprecated;

    /**
     * 延迟注册服务时间(毫秒) ，设为-1时，表示延迟到Spring容器初始化完成时暴露服务
     */
    protected Integer delayed;

    /**
     * 是否暴露服务
     */
    protected Boolean export;

    /**
     * 权重
     */
    protected Integer weight;

    /**
     * 服务文档URL
     */
    protected String document;

    /**
     * 服务是否动态注册，如果设为false，注册后将显示后disable状态，
     * 需人工启用，并且服务提供者停止时，也不会自动取消册，需人工禁用。
     */
    protected Boolean dynamic;

    /**
     * 令牌验证，为空表示不开启，如果为true，表示随机生成动态令牌，否则使用静态令牌，
     * 令牌的作用是防止消费者绕过注册中心直接访问，保证注册中心的授权功能有效，
     * 如果使用点对点调用，需关闭令牌功能
     */
    protected String token;

    /**
     * 设为true，将向logger中输出访问日志，
     * 也可填写访问日志文件路径，直接把访问日志输出到指定文件
     */
    protected String accesslog;

    /**
     * 使用指定的协议暴露服务，在多协议时使用，
     * 值为<dubbo:protocol>的id属性，多个协议ID用逗号分隔
     */
    protected List<ProtocolConfig> protocols;

    /**
     * 服务提供者每服务每方法最大可并行执行请求数
     */
    private Integer executes;

    /**
     * 该协议的服务是否注册到注册中心
     */
    private Boolean register;

    /**
     * warm up period
     */
    private Integer warmup;

    private String serialization;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        checkKey("version", version);
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        checkKey("group", group);
        this.group = group;
    }

    public Boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Integer getDelayed() {
        return delayed;
    }

    public void setDelayed(Integer delayed) {
        this.delayed = delayed;
    }

    public Boolean getExport() {
        return export;
    }

    public void setExport(Boolean export) {
        this.export = export;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Parameter(escaped = true)
    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public Boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(Boolean dynamic) {
        this.dynamic = dynamic;
    }

    public String getToken() {
        return token;
    }

    public void setToken(Boolean token) {
        if (token == null) {
            setToken((String) null);
        } else {
            setToken(String.valueOf(token));
        }
    }

    public void setToken(String token) {
        checkName("token", token);
        this.token = token;
    }

    public String getAccesslog() {
        return accesslog;
    }

    public void setAccesslog(String accesslog) {
        this.accesslog = accesslog;
    }

    public void setAccesslog(Boolean accesslog) {
        if (accesslog == null) {
            setAccesslog((String) null);
        } else {
            setAccesslog(String.valueOf(accesslog));
        }
    }

    public List<ProtocolConfig> getProtocols() {
        return protocols;
    }

    @SuppressWarnings("unchecked")
    public void setProtocols(List<? extends ProtocolConfig> protocols) {
        this.protocols = (List<ProtocolConfig>) protocols;
    }

    public ProtocolConfig getProtocol() {
        return protocols == null || protocols.isEmpty() ? null : protocols.get(0);
    }

    public void setProtocol(ProtocolConfig protocol) {
        this.protocols = Arrays.asList(protocol);
    }

    public Integer getExecutes() {
        return executes;
    }

    public void setExecutes(Integer executes) {
        this.executes = executes;
    }

    public Boolean isRegister() {
        return register;
    }

    public void setRegister(Boolean register) {
        this.register = register;
    }

    public Integer getWarmup() {
        return warmup;
    }

    public void setWarmup(Integer warmup) {
        this.warmup = warmup;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    @Override
    @Parameter(key = Constants.SERVICE_FILTER_KEY, append = true)
    public String getFilter() {
        return super.getFilter();
    }

    @Override
    @Parameter(key = Constants.EXPORTER_LISTENER_KEY, append = true)
    public String getListener() {
        return listener;
    }

    @Override
    public void setListener(String listener) {
        checkMultiExtension(ExporterListener.class, "listener", listener);
        this.listener = listener;
    }
}
