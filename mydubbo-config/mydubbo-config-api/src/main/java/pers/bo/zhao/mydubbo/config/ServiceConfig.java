package pers.bo.zhao.mydubbo.config;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.utils.CollectionUtils;
import pers.bo.zhao.mydubbo.common.utils.NamedThreadFactory;
import pers.bo.zhao.mydubbo.common.utils.StringUtils;
import pers.bo.zhao.mydubbo.rpc.service.GenericService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Bo.Zhao
 * @since 19/2/12
 */
public class ServiceConfig<T> extends AbstractServiceConfig {

    private static final long serialVersionUID = 3033787999037024738L;

    private static final ScheduledExecutorService DELAY_EXPORT_EXECUTOR = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("MyDubboServiceDelayExporter", true));

    private String interfaceName;
    private Class<?> interfaceClass;
    private T ref;
    /**
     * 服务路径，缺省为接口名
     */
    private String path;
    private List<MethodConfig> methods;

    private ProviderConfig provider;

    private transient volatile boolean exported;

    private transient volatile boolean unexported;

    private volatile String generic;

    public synchronized void export() {
        // provider不为null
        if (provider != null) {
            if (export == null) {
                export = provider.getExport();
            }
            if (delayed == null) {
                delayed = provider.getDelayed();
            }
        }

        // 服务不导出
        if (export != null && !export) {
            return;
        }

        // 延时导出服务
        if (delayed > 0) {
            DELAY_EXPORT_EXECUTOR.schedule(this::doExport, delayed, TimeUnit.MILLISECONDS);
        } else {
            // 立即导出服务
            doExport();
        }
    }

    private synchronized void doExport() {
        if (unexported) {
            throw new IllegalStateException("MyDubbo already unexported!");
        }
        if (exported) {
            return;
        }
        exported = true;
        // 检测interfaceName的合法性
        if (StringUtils.isEmpty(interfaceName)) {
            throw new IllegalStateException("<mydubbo:service interface=\"\" /> interface not allow null");
        }
        checkProviderDefault();

        // 下面几个 if 语句用于检测application、registries、protocols等核心配置类对象是否为空，
        // 若为空，则尝试从其他配置类对象中获取相应的实例。
        if (provider != null) {
            if (application == null) {
                application = provider.getApplication();
            }
//            if (module == null) {
//                module = provider.getModule();
//            }
            if (registries == null) {
                registries = provider.getRegistries();
            }
            if (protocols == null) {
                protocols = provider.getProtocols();
            }
        }
//        if (module != null) {
//            if (registries == null) {
//                registries = module
//            }
//        }
        if (application != null) {
            if (registries == null) {
                registries = application.getRegistries();
            }
        }
        // 泛化调用
        if (ref instanceof GenericService) {
            interfaceClass = GenericService.class;
            if (StringUtils.isEmpty(generic)) {
                generic = Boolean.TRUE.toString();
            }
        } else {
            try {
                interfaceClass = Class.forName(interfaceName, true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            checkInterfaceAndMethods(interfaceClass, methods);
            checkRef();
            generic = Boolean.FALSE.toString();
        }

        checkApplication();
        checkRegistry();
        checkProtocol();
        appendProperties(this);
        if (StringUtils.isEmpty(path)) {
            path = interfaceName;
        }
        doExportUrls();


    }

    private void doExportUrls() {
        List<URL> registriesURLs = loadRegistries(true);
        for (ProtocolConfig protocolConfig : protocols) {
            doExportUrlForOneProtocol(protocolConfig, registriesURLs);
        }
    }

    private void doExportUrlForOneProtocol(ProtocolConfig protocolConfig, List<URL> registriesURLs) {

    }

    private void checkProtocol() {
        if ((CollectionUtils.isEmpty(protocols)) && provider != null) {
            setProtocols(provider.getProtocols());
        }
        if (CollectionUtils.isEmpty(protocols)) {
            setProtocol(new ProtocolConfig());
        }
        for (ProtocolConfig protocolConfig : protocols) {
            if (StringUtils.isEmpty(protocolConfig.getName())) {
                protocolConfig.setName(Constants.MYDUBBO_VERSION_KEY);
            }
            appendProperties(protocolConfig);
        }
    }

    private void checkRef() {
        if (ref == null) {
            throw new IllegalStateException("ref not allow null!");
        }
        if (!interfaceClass.isInstance(ref)) {
            throw new IllegalStateException("The class " + ref.getClass().getName() +
                    " unimplemented interface " + interfaceClass + "!");
        }
    }

    /**
     * 检查provider的合法性，如果不合法则新建一个provider，
     * 并通过系统变量为其初始化
     */
    private void checkProviderDefault() {
        if (provider == null) {
            provider = new ProviderConfig();
        }
        appendProperties(provider);
    }
}
