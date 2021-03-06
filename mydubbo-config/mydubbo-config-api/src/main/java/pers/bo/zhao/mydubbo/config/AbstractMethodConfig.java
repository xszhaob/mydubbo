package pers.bo.zhao.mydubbo.config;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.config.support.Parameter;
import pers.bo.zhao.mydubbo.rpc.cluster.LoadBalance;

import java.util.Map;

/**
 * @author Bo.Zhao
 * @since 19/2/12
 */
public abstract class AbstractMethodConfig extends AbstractConfig {

    private static final long serialVersionUID = 1L;

    /**
     * 超时时间
     */
    protected Integer timeout;
    /**
     * 重试次数
     */
    protected Integer retries;
    /**
     * max current invocations
     */
    protected Integer actives;
    /**
     * 负载均衡
     */
    private String loadbalance;

    /**
     * 是否异步
     */
    protected Boolean async;

    /**
     * 异步发送，是否需要ack返回
     */
    protected Boolean sent;

    protected String mock;

    protected String merger;

    protected String cache;

    protected String validation;

    protected Map<String, String> parameters;


    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getActives() {
        return actives;
    }

    public void setActives(Integer actives) {
        this.actives = actives;
    }

    public String getLoadbalance() {
        return loadbalance;
    }

    public void setLoadbalance(String loadbalance) {
        checkExtension(LoadBalance.class, "loadbalance", loadbalance);
        this.loadbalance = loadbalance;
    }

    public Boolean isAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    // TODO: 2019/3/4 含义？
    @Parameter(escaped = true)
    public String getMock() {
        return mock;
    }

    public void setMock(Boolean mock) {
        if (mock == null) {
            setMock((String)null);
        } else {
            setMock(String.valueOf(mock));
        }
    }

    public void setMock(String mock) {
        if (mock != null && mock.startsWith(Constants.RETURN_PREFIX)) {
            checkLength("mock", mock);
        } else {
            checkName("mock", mock);
        }
        this.mock = mock;
    }

    public String getMerger() {
        return merger;
    }

    public void setMerger(String merger) {
        this.merger = merger;
    }

    public String getCache() {
        return cache;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        checkParameters(parameters);
        this.parameters = parameters;
    }
}
