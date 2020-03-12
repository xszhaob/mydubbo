package pers.bo.zhao.mydubbo.registry.zookeeper;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;
import pers.bo.zhao.mydubbo.common.utils.UrlUtils;
import pers.bo.zhao.mydubbo.registry.NotifyListener;
import pers.bo.zhao.mydubbo.registry.support.FailbackRegistry;
import pers.bo.zhao.mydubbo.remoting.zookeeper.ChildListener;
import pers.bo.zhao.mydubbo.remoting.zookeeper.StateListener;
import pers.bo.zhao.mydubbo.remoting.zookeeper.ZookeeperClient;
import pers.bo.zhao.mydubbo.remoting.zookeeper.ZookeeperTransporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ZookeeperRegistry extends FailbackRegistry {
    private final static Logger logger = LoggerFactory.getLogger(ZookeeperRegistry.class);

    private final static int DEFAULT_ZOOKEEPER_PORT = 2181;

    private final static String DEFAULT_ROOT = "dubbo";

    private final String root;

    private final ZookeeperClient zkClient;


    private final ConcurrentMap<URL, ConcurrentMap<NotifyListener, ChildListener>> zkListeners = new ConcurrentHashMap<>();


    public ZookeeperRegistry(URL url, ZookeeperTransporter zookeeperTransporter) {
        super(url);

        if (url.isAnyHost()) {
            throw new IllegalStateException("registry address == null");
        }

        // 分组
        String group = url.getParameter(Constants.GROUP_KEY, DEFAULT_ROOT);
        if (!group.startsWith(Constants.PATH_SEPARATOR)) {
            group = Constants.PATH_SEPARATOR + group;
        }

        this.root = group;
        zkClient = zookeeperTransporter.connect(url);
        zkClient.addStateListener(state -> {
            if (state == StateListener.RECONNECTED) {
                try {
                    recover();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            zkClient.close();
        } catch (Exception e) {
            logger.warn("Failed to close zookeeper client " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doRegister(URL url) {
        zkClient.create(toUrlPath(url), url.getParameter(Constants.DYNAMIC_KEY, true));
    }

    @Override
    protected void doUnregister(URL url) {
        zkClient.delete(toUrlPath(url));
    }

    @Override
    protected void doSubscribe(URL url, NotifyListener listener) {
        List<URL> urls = new ArrayList<>();

        for (String path : toCategoriesPath(url)) {
            ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
            if (listeners == null) {
                zkListeners.putIfAbsent(url, new ConcurrentHashMap<>());
                listeners = zkListeners.get(url);
            }

            ChildListener childListener = listeners.get(listener);
            if (childListener == null) {
                listeners.putIfAbsent(listener,
                        (path1, children) -> ZookeeperRegistry.this.notify(
                                url,
                                listener,
                                toUrlsWithEmpty(url, path1, children)));
                childListener = listeners.get(listener);
            }

            // 永久节点
            zkClient.create(path, false);

            List<String> children = zkClient.addChildListener(path, childListener);
            if (children != null) {
                urls.addAll(toUrlsWithEmpty(url, path, children));
            }

            notify(url, listener, urls);
        }
    }

    @Override
    protected void doUnsubscribe(URL url, NotifyListener listener) {
        ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
        if (listeners != null) {
            ChildListener zkListener = listeners.get(listener);
            for (String path : toCategoriesPath(url)) {
                zkClient.removeChildListener(path, zkListener);
            }
        }
    }

    @Override
    public boolean isAvailable() {
        return zkClient.isConnected();
    }


    private String toRootDir() {
        if (root.equals(Constants.PATH_SEPARATOR)) {
            return root;
        }
        return root + Constants.PATH_SEPARATOR;
    }

    private String toRootPath() {
        return root;
    }

    private String toServicePath(URL url) {
        String name = url.getServiceInterface();
        if (Constants.ANY_VALUE.equals(name)) {
            return toRootPath();
        }
        return toRootDir() + URL.encode(name);
    }

    private String[] toCategoriesPath(URL url) {
        String[] categories = url.getParameter(Constants.CATEGORY_KEY, new String[]{Constants.DEFAULT_CATEGORY});
        String[] paths = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            paths[i] = toServicePath(url) + Constants.PATH_SEPARATOR + categories[i];
        }
        return paths;
    }

    private String toCategoryPath(URL url) {
        return toServicePath(url) + Constants.PATH_SEPARATOR + url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
    }

    private String toUrlPath(URL url) {
        return toCategoryPath(url) + Constants.PATH_SEPARATOR + URL.encode(url.toFullString());
    }

    private List<URL> toUrlsWithoutEmpty(URL consumer, List<String> providers) {
        List<URL> urls = new ArrayList<>();
        if (providers != null && !providers.isEmpty()) {
            for (String provider : providers) {
                provider = URL.decode(provider);
                if (provider.contains("://")) {
                    URL url = URL.valueOf(provider);
                    if (UrlUtils.isMatch(consumer, url)) {
                        urls.add(url);
                    }
                }
            }
        }
        return urls;
    }

    private List<URL> toUrlsWithEmpty(URL consumer, String path, List<String> providers) {
        List<URL> urls = toUrlsWithoutEmpty(consumer, providers);
        if (urls == null || urls.isEmpty()) {
            int i = path.lastIndexOf('/');
            String category = i < 0 ? path : path.substring(i + 1);
            URL empty = consumer.setProtocol(Constants.EMPTY_PROTOCOL).addParameter(Constants.CATEGORY_KEY, category);
            urls.add(empty);
        }
        return urls;
    }
}
