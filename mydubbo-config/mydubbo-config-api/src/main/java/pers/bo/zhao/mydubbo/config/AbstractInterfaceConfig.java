package pers.bo.zhao.mydubbo.config;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.config.support.Parameter;
import pers.bo.zhao.mydubbo.rpc.Filter;
import pers.bo.zhao.mydubbo.rpc.InvokeListener;
import pers.bo.zhao.mydubbo.rpc.cluster.Cluster;

import java.util.List;

/**
 * @author Bo.Zhao
 * @since 19/2/12
 */
public abstract class AbstractInterfaceConfig extends AbstractMethodConfig {
    private static final long serialVersionUID = -1559314110797223229L;

    /**
     * 设为true，表示使用缺省代理类名，
     * 即：接口名 + Stub后缀，服务接口客户端本地代理类名，
     * 用于在客户端执行本地逻辑，如本地缓存等，
     * 该本地代理类的构造函数必须允许传入远程代理对象，
     * 构造函数如：public XxxServiceStub(XxxService xxxService)
     */
    protected String stub;

    /**
     * proxy type
     */
    protected String proxy;

    /**
     * cluster type
     */
    protected String cluster;

    /**
     * filter
     */
    protected String filter;

    /**
     * listener
     */
    protected String listener;

    protected String owner;

    /**
     * 连接数限制。
     * 0代表共享连接；
     * 其他值则是指定了当前service的委托连接数？
     */
    protected Integer connections;

    /**
     * layer
     */
    protected String layer;

    protected ApplicationConfig application;

    protected ModuleConfig module;

    protected List<RegistryConfig> registries;

    protected String onconnect;

    protected String ondisconnect;

    protected Integer callbacks;

    /**
     * 用于service暴露的scope，如果是local表示仅在当前JVM中查找
     */
    private String scope;


    public String getStub() {
        return stub;
    }

    public void setStub(Boolean stub) {
        if (stub == null) {
            setStub((String) null);
        } else {
            setStub(String.valueOf(stub));
        }
    }

    public void setStub(String stub) {
        checkName("stub", stub);
        this.stub = stub;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        checkExtension(Cluster.class, "cluster", cluster);
        this.cluster = cluster;
    }

    @Parameter(key = Constants.REFERENCE_FILTER_KEY, append = true)
    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        checkMultiExtension(Filter.class, "filter", filter);
        this.filter = filter;
    }

    @Parameter(key = Constants.INVOKER_LISTENER_KEY, append = true)
    public String getListener() {
        return listener;
    }

    public void setListener(String listener) {
        checkMultiExtension(InvokeListener.class, "listener", listener);
        this.listener = listener;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        checkMultiName("owner", owner);
        this.owner = owner;
    }

    public Integer getConnections() {
        return connections;
    }

    public void setConnections(Integer connections) {
        this.connections = connections;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        checkNameHasSymbol("layer", layer);
        this.layer = layer;
    }

    public ApplicationConfig getApplication() {
        return application;
    }

    public void setApplication(ApplicationConfig application) {
        this.application = application;
    }

    public ModuleConfig getModule() {
        return module;
    }

    public void setModule(ModuleConfig module) {
        this.module = module;
    }

    public List<RegistryConfig> getRegistries() {
        return registries;
    }

    public void setRegistries(List<RegistryConfig> registries) {
        this.registries = registries;
    }

    public String getOnconnect() {
        return onconnect;
    }

    public void setOnconnect(String onconnect) {
        this.onconnect = onconnect;
    }

    public String getOndisconnect() {
        return ondisconnect;
    }

    public void setOndisconnect(String ondisconnect) {
        this.ondisconnect = ondisconnect;
    }

    public Integer getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(Integer callbacks) {
        this.callbacks = callbacks;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
