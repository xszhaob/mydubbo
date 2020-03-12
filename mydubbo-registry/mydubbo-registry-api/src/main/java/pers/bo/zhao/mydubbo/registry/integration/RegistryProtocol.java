package pers.bo.zhao.mydubbo.registry.integration;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.extension.ExtensionLoader;
import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;
import pers.bo.zhao.mydubbo.common.utils.CollectionUtils;
import pers.bo.zhao.mydubbo.common.utils.ConfigUtils;
import pers.bo.zhao.mydubbo.common.utils.NamedThreadFactory;
import pers.bo.zhao.mydubbo.common.utils.UrlUtils;
import pers.bo.zhao.mydubbo.registry.NotifyListener;
import pers.bo.zhao.mydubbo.registry.Registry;
import pers.bo.zhao.mydubbo.registry.RegistryFactory;
import pers.bo.zhao.mydubbo.registry.support.ProviderConsumerRegTable;
import pers.bo.zhao.mydubbo.rpc.Exporter;
import pers.bo.zhao.mydubbo.rpc.Invoker;
import pers.bo.zhao.mydubbo.rpc.Protocol;
import pers.bo.zhao.mydubbo.rpc.RpcException;
import pers.bo.zhao.mydubbo.rpc.cluster.Configurator;
import pers.bo.zhao.mydubbo.rpc.protocol.InvokerWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistryProtocol implements Protocol {
    private static final Logger logger = LoggerFactory.getLogger(RegistryProtocol.class);

    private static RegistryProtocol INSTANCE;

    private final Map<URL, NotifyListener> overrideListeners = new ConcurrentHashMap<URL, NotifyListener>();


    // <dubboProviderUrl,ExporterChangeableWrapper>
    private final Map<String, ExporterChangeableWrapper<?>> bounds = new ConcurrentHashMap<>();

    private Protocol protocol;

    private RegistryFactory registryFactory;

    public RegistryProtocol() {
        INSTANCE = this;
    }

    public static RegistryProtocol getRegistryProtocol() {
        if (INSTANCE == null) {
            ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(Constants.REGISTRY_PROTOCOL); // load
        }
        return INSTANCE;
    }

    @Override
    public int getDefaultPort() {
        return 8090;
    }

    /**
     * 在ExtensionLoader的
     * {@link pers.bo.zhao.mydubbo.common.extension.ExtensionLoader#injectExtension(java.lang.Object)}方法完成注入
     *
     * @param protocol
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }


    public void setRegistryFactory(RegistryFactory registryFactory) {
        this.registryFactory = registryFactory;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> originInvoker) throws RpcException {
        // 暴露服务，通过Netty暴露本地的服务
        ExporterChangeableWrapper<T> exporter = doLocalExport(originInvoker);

        // ***************注册服务*****************
        // 获取注册中心
        URL registryUrl = getRegistryUrl(originInvoker);

        // 根据URL获取注册中心实现
        Registry registry = getRegistry(originInvoker);
        // 获取已注册的服务提供者
        URL registedProviderUrl = getRegistedProviderUrl(originInvoker);
        // 向消费者和提供者注册表中注册服务提供者
        ProviderConsumerRegTable.registerProvider(originInvoker, registryUrl, registedProviderUrl);

        boolean register = registedProviderUrl.getParameter(Constants.REGISTER_KEY, true);
        // 根据register判断是否需要注册服务
        if (register) {
            register(registryUrl, registedProviderUrl);
            ProviderConsumerRegTable.getProviderWrapper(originInvoker).setReg(true);
        }


        // ****************订阅注册中心数据*******************
        // 获取订阅地址
        URL subscribedOverrideUrl = getSubscribedOverrideUrl(registedProviderUrl);
        // 创建监听器
        final OverrideListener overrideSubscribeListener = new OverrideListener(subscribedOverrideUrl, originInvoker);
        overrideListeners.put(subscribedOverrideUrl, overrideSubscribeListener);

        // 向注册中心进行订阅 override 数据
        registry.subscribe(subscribedOverrideUrl, overrideSubscribeListener);

        // **************返回数据*****************
        // 创建并返回 DestroyableExporter
        return new DestroyableExporter<>(exporter, originInvoker, subscribedOverrideUrl, registedProviderUrl);
    }

    private Registry getRegistry(Invoker<?> originInvoker) {
        URL registryUrl = getRegistryUrl(originInvoker);
        return registryFactory.getRegistry(registryUrl);
    }

    private URL getRegistedProviderUrl(final Invoker<?> originInvoker) {
        URL providerUrl = getProviderUrl(originInvoker);
        //The address you see at the registry
        final URL registedProviderUrl = providerUrl.removeParameters(getFilteredKeys(providerUrl))
                .removeParameter(Constants.MONITOR_KEY)
                .removeParameter(Constants.BIND_IP_KEY)
                .removeParameter(Constants.BIND_PORT_KEY)
                .removeParameter(Constants.QOS_ENABLE)
                .removeParameter(Constants.QOS_PORT)
                .removeParameter(Constants.ACCEPT_FOREIGN_IP)
                .removeParameter(Constants.VALIDATION_KEY)
                .removeParameter(Constants.INTERFACES);
        return registedProviderUrl;
    }

    private URL getSubscribedOverrideUrl(URL registedProviderUrl) {
        return registedProviderUrl.setProtocol(Constants.PROVIDER_PROTOCOL)
                .addParameters(Constants.CATEGORY_KEY, Constants.CONFIGURATORS_CATEGORY,
                        Constants.CHECK_KEY, String.valueOf(false));
    }

    private static String[] getFilteredKeys(URL url) {
        Map<String, String> params = url.getParameters();
        if (params != null && !params.isEmpty()) {
            List<String> filteredKeys = new ArrayList<String>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry != null && entry.getKey() != null && entry.getKey().startsWith(Constants.HIDE_KEY_PREFIX)) {
                    filteredKeys.add(entry.getKey());
                }
            }
            return filteredKeys.toArray(new String[filteredKeys.size()]);
        } else {
            return new String[]{};
        }
    }

    private URL getRegistryUrl(Invoker<?> originInvoker) {
        URL registryUrl = originInvoker.getUrl();
        if (Constants.REGISTRY_PROTOCOL.equals(registryUrl.getProtocol())) {
            String protocol = registryUrl.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_DIRECTORY);
            registryUrl = registryUrl.setProtocol(protocol).removeParameter(Constants.REGISTRY_KEY);
        }
        return registryUrl;
    }

    private void register(URL registryUrl, URL registedProviderUrl) {
        Registry registry = registryFactory.getRegistry(registryUrl);
        registry.register(registedProviderUrl);
    }

    @SuppressWarnings("unchecked")
    private <T> ExporterChangeableWrapper<T> doLocalExport(Invoker<T> originInvoker) {
        // providerUrl
        String key = getCacheKey(originInvoker);

        ExporterChangeableWrapper<T> exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
        if (exporter == null) {
            synchronized (bounds) {
                exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
                if (exporter == null) {
                    Invoker<T> invokerDelegete = new InvokerDelegate<>(originInvoker, getProviderUrl(originInvoker));
                    exporter = new ExporterChangeableWrapper<>(invokerDelegete, protocol.export(invokerDelegete));
                    bounds.put(key, exporter);
                }
            }
        }
        return exporter;
    }

    private <T> void doChangeLocalExport(final Invoker<T> originInvoker, URL newInvokerUrl) {
        String key = getCacheKey(originInvoker);
        final ExporterChangeableWrapper<T> exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
        if (exporter == null) {
            logger.warn(new IllegalStateException("error state, exporter should not be null"));
        } else {
            final Invoker<T> invokerDelegete = new InvokerDelegate<T>(originInvoker, newInvokerUrl);
            exporter.setExporter(protocol.export(invokerDelegete));
        }
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        return null;
    }

    @Override
    public void destroy() {

    }

    private String getCacheKey(final Invoker<?> originInvoker) {
        URL providerUrl = getProviderUrl(originInvoker);
        String key = providerUrl.removeParameters("dynamic", "enabled").toFullString();
        return key;
    }

    private URL getProviderUrl(final Invoker<?> originInvoker) {
        String export = originInvoker.getUrl().getParameterAndDecoded(Constants.EXPORT_KEY);
        if (export == null || export.length() == 0) {
            throw new IllegalArgumentException("The registry export url is null! registry: " + originInvoker.getUrl());
        }

        URL providerUrl = URL.valueOf(export);
        return providerUrl;
    }

    private class ExporterChangeableWrapper<T> implements Exporter<T> {
        private final Invoker<T> originInvoker;
        private Exporter<T> exporter;

        public ExporterChangeableWrapper(Invoker<T> originInvoker, Exporter<T> exporter) {
            this.originInvoker = originInvoker;
            this.exporter = exporter;
        }

        public Invoker<T> getOriginInvoker() {
            return originInvoker;
        }

        public void setExporter(Exporter<T> exporter) {
            this.exporter = exporter;
        }

        @Override
        public Invoker<T> getInvoker() {
            return exporter.getInvoker();
        }

        @Override
        public void unexport() {
            String key = getCacheKey(this.originInvoker);
            bounds.remove(key);
            exporter.unexport();
        }
    }


    public class InvokerDelegate<T> extends InvokerWrapper<T> {
        private final Invoker<T> invoker;


        public InvokerDelegate(Invoker<T> invoker, URL url) {
            super(invoker, url);
            this.invoker = invoker;
        }


        public Invoker<T> getInvoker() {
            if (invoker instanceof InvokerDelegate) {
                return ((InvokerDelegate<T>) invoker).getInvoker();
            } else {
                return invoker;
            }
        }
    }

    private class OverrideListener implements NotifyListener {
        private final URL subscribeUrl;
        private final Invoker originInvoker;

        public OverrideListener(URL subscribeUrl, Invoker originInvoker) {
            this.subscribeUrl = subscribeUrl;
            this.originInvoker = originInvoker;
        }

        @Override
        public void notify(List<URL> urls) {
            List<URL> matchedUrls = getMatchedUrls(urls, subscribeUrl);
            if (CollectionUtils.isEmpty(matchedUrls)) {
                return;
            }

            List<Configurator> configurators = RegistryDirectory.toConfigurators(matchedUrls);

            final Invoker<?> invoker;
            if (originInvoker instanceof InvokerDelegate) {
                invoker = ((InvokerDelegate<?>) originInvoker).getInvoker();
            } else {
                invoker = originInvoker;
            }
            //The origin invoker
            URL originUrl = RegistryProtocol.this.getProviderUrl(invoker);
            String key = getCacheKey(originInvoker);
            ExporterChangeableWrapper<?> exporter = bounds.get(key);
            if (exporter == null) {
                logger.warn(new IllegalStateException("error state, exporter should not be null"));
                return;
            }
            //The current, may have been merged many times
            URL currentUrl = exporter.getInvoker().getUrl();
            //Merged with this configuration
            URL newUrl = getConfigedInvokerUrl(configurators, originUrl);
            if (!currentUrl.equals(newUrl)) {
                RegistryProtocol.this.doChangeLocalExport(originInvoker, newUrl);
                logger.info("exported provider url changed, origin url: " + originUrl + ", old export url: " + currentUrl + ", new export url: " + newUrl);
            }
        }

        private List<URL> getMatchedUrls(List<URL> configuratorUrls, URL currentSubscribe) {
            List<URL> result = new ArrayList<>();
            for (URL url : configuratorUrls) {
                // Check whether url is to be applied to the current service
                if (UrlUtils.isMatch(currentSubscribe, url)) {
                    result.add(url);
                }
            }
            return result;
        }

        //Merge the urls of configurators
        private URL getConfigedInvokerUrl(List<Configurator> configurators, URL url) {
            for (Configurator configurator : configurators) {
                url = configurator.configure(url);
            }
            return url;
        }
    }

    static private class DestroyableExporter<T> implements Exporter<T> {

        public static final ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("Exporter-Unexport", true));

        private Exporter<T> exporter;
        private Invoker<T> originInvoker;
        private URL subscribeUrl;
        private URL registerUrl;

        public DestroyableExporter(Exporter<T> exporter, Invoker<T> originInvoker, URL subscribeUrl, URL registerUrl) {
            this.exporter = exporter;
            this.originInvoker = originInvoker;
            this.subscribeUrl = subscribeUrl;
            this.registerUrl = registerUrl;
        }

        @Override
        public Invoker<T> getInvoker() {
            return exporter.getInvoker();
        }

        @Override
        public void unexport() {
            Registry registry = RegistryProtocol.INSTANCE.getRegistry(originInvoker);
            try {
                registry.unregister(registerUrl);
            } catch (Throwable t) {
                logger.warn(t.getMessage(), t);
            }
            try {
                NotifyListener listener = RegistryProtocol.INSTANCE.overrideListeners.remove(subscribeUrl);
                registry.unsubscribe(subscribeUrl, listener);
            } catch (Throwable t) {
                logger.warn(t.getMessage(), t);
            }

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        int timeout = ConfigUtils.getServerShutdownTimeout();
                        if (timeout > 0) {
                            logger.info("Waiting " + timeout + "ms for registry to notify all consumers before unexport. Usually, this is called when you use dubbo API");
                            Thread.sleep(timeout);
                        }
                        exporter.unexport();
                    } catch (Throwable t) {
                        logger.warn(t.getMessage(), t);
                    }
                }
            });
        }
    }
}
