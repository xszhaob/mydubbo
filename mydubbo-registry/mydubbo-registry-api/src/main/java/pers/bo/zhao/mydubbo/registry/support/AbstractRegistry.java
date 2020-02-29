package pers.bo.zhao.mydubbo.registry.support;

import pers.bo.zhao.mydubbo.common.Constants;
import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;
import pers.bo.zhao.mydubbo.common.utils.*;
import pers.bo.zhao.mydubbo.registry.NotifyListener;
import pers.bo.zhao.mydubbo.registry.Registry;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractRegistry implements Registry {
    private static final char URL_SEPARATOR = ' ';
    private static final String URL_SPLIT = "\\s+";

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegistry.class);

    private final Properties properties = new Properties();
    private final ExecutorService registryCacheExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("MyDubboSaveRegistryCache", true));
    /**
     * 同步保存文件
     */
    private final boolean syncSaveFile;
    private final AtomicLong lastCacheChanged = new AtomicLong();
    private final Set<URL> registered = new ConcurrentHashSet<>();
    private final ConcurrentHashMap<URL, Set<NotifyListener>> subscribed = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<URL, Map<String, List<URL>>> notified = new ConcurrentHashMap<>();
    private URL registryUrl;

    private File file;


    public AbstractRegistry(URL url) {
        // 设置注册的url
        setUrl(url);

        syncSaveFile = false;
        String fileName = url.getParameter(Constants.FILE_KEY,
                System.getProperty("user.home") +
                        "/.mydubbo/mydubbo-registry-" +
                        url.getParameter(Constants.APPLICATION_KEY) +
                        "-" + url.getAddress() + ".cache");
        File file = null;
        if (ConfigUtils.isNotEmpty(fileName)) {
            file = new File(fileName);
            // 文件不存在 并且 上一级文件不等于null 并且上一级文件不存在
            if (!file.exists() && file.getParentFile() != null && !file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new IllegalArgumentException("Invalid registry store file " + file + ", cause: Failed to create directory " + file.getParentFile() + "!");
                }
            }
        }
        this.file = file;
        // 加载配置文件
        loadProperties();
        // 通知
        notify(url.getBackupUrls());
    }

    /**
     * 如果urls为空，则生成一个protocol为empty的url并返回
     */
    private List<URL> filterEmpty(URL url, List<URL> urls) {
        if (CollectionUtils.isNotEmpty(urls)) {
            return urls;
        }
        List<URL> result = new ArrayList<>(1);
        result.add(url.setProtocol(Constants.EMPTY_PROTOCOL));
        return result;
    }

    @Override
    public URL getUrl() {
        return registryUrl;
    }

    public void setUrl(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("registry url == null");
        }
        this.registryUrl = url;
    }

    public Set<URL> getRegistered() {
        return registered;
    }

    public ConcurrentHashMap<URL, Set<NotifyListener>> getSubscribed() {
        return subscribed;
    }

    public ConcurrentHashMap<URL, Map<String, List<URL>>> getNotified() {
        return notified;
    }

    public File getCacheFile() {
        return file;
    }

    public Properties getCacheProperties() {
        return properties;
    }

    public AtomicLong getLastCacheChanged() {
        return lastCacheChanged;
    }

    public void doSaveProperties(long version) {
        if (version < lastCacheChanged.get()) {
            return;
        }
        if (file == null) {
            return;
        }
        // Save
        try {
            File lockfile = new File(file.getAbsolutePath() + ".lock");
            if (!lockfile.exists()) {
                lockfile.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(lockfile, "rw");
            try {
                FileChannel channel = raf.getChannel();
                try {
                    FileLock lock = channel.tryLock();
                    if (lock == null) {
                        throw new IOException("Can not lock the registry cache file " + file.getAbsolutePath() + ", ignore and retry later, maybe multi java process use the file, please config: dubbo.registry.file=xxx.properties");
                    }
                    // Save
                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileOutputStream outputFile = new FileOutputStream(file);
                        try {
                            properties.store(outputFile, "MyDubbo Registry Cache");
                        } finally {
                            outputFile.close();
                        }
                    } finally {
                        lock.release();
                    }
                } finally {
                    channel.close();
                }
            } finally {
                raf.close();
            }
        } catch (Throwable e) {
            if (version < lastCacheChanged.get()) {
                return;
            } else {
                registryCacheExecutor.execute(new SaveProperties(lastCacheChanged.incrementAndGet()));
            }
            LOGGER.warn("Failed to save registry store file, cause: " + e.getMessage(), e);
        }
    }

    private void loadProperties() {
        if (file != null && file.exists()) {
            try (InputStream inputStream = new FileInputStream(file)) {
                properties.load(inputStream);
            } catch (Throwable throwable) {
                LOGGER.warn("Failed to load registry store file " + file, throwable);
            }
        }
    }


    public List<URL> getCacheUrls(URL url) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            if (StringUtils.isNotEmpty(key)
                    && key.equals(url.getServiceKey())
                    && (Character.isLetter(key.charAt(0)) || key.charAt(0) == '_')
                    && StringUtils.isNotEmpty(value)) {
                String[] arr = value.trim().split(URL_SPLIT);
                List<URL> urls = new ArrayList<>(arr.length);
                for (String u : arr) {
                    urls.add(URL.valueOf(u));
                }
                return urls;
            }
        }
        return null;
    }


    @Override
    public List<URL> lookup(URL url) {
        List<URL> result = new ArrayList<>();
        Map<String, List<URL>> notifiedUrls = getNotified().get(url);
        if (CollectionUtils.isNotEmpty(notifiedUrls)) {
            for (List<URL> urls : notifiedUrls.values()) {
                for (URL u : urls) {
                    if (!Constants.EMPTY_PROTOCOL.equals(u.getProtocol())) {
                        result.add(u);
                    }
                }
            }
        } else {
            final AtomicReference<List<URL>> reference = new AtomicReference<>();
            NotifyListener listener = reference::set;
            subscribe(url, listener);
            List<URL> urls = reference.get();
            if (CollectionUtils.isNotEmpty(urls)) {
                for (URL u : urls) {
                    if (!Constants.EMPTY_PROTOCOL.equals(u.getProtocol())) {
                        result.add(u);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void register(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("register url == null");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Register: " + url);
        }
        registered.add(url);
    }

    @Override
    public void unregister(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("unregister url == null");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Unregister: " + url);
        }
        registered.remove(url);
    }


    @Override
    public void subscribe(URL url, NotifyListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("subscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("subscribe listener == null");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Subscribe: " + url);
        }

        Set<NotifyListener> listeners = subscribed.get(url);
        if (listeners == null) {
            subscribed.putIfAbsent(url, new HashSet<>());
            listeners = subscribed.get(url);
        }
        listeners.add(listener);
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("unsubscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("unsubscribe listener == null");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Unsubscribe: " + url);
        }

        Set<NotifyListener> listeners = subscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    protected void recover() {
        // registry
        Set<URL> recoverRegistered = new HashSet<>(getRegistered());
        if (CollectionUtils.isNotEmpty(recoverRegistered)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Recover register url " + recoverRegistered);
            }
            for (URL url : recoverRegistered) {
                register(url);
            }
        }

        // subscribe
        Map<URL, Set<NotifyListener>> recoverSubscribed = new HashMap<>(getSubscribed());
        if (CollectionUtils.isNotEmpty(recoverSubscribed)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Recover subscribe url " + recoverSubscribed.keySet());
            }
            for (Map.Entry<URL, Set<NotifyListener>> entry : recoverSubscribed.entrySet()) {
                URL url = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    subscribe(url, listener);
                }
            }
        }
    }

    public void notify(List<URL> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            return;
        }
        for (Map.Entry<URL, Set<NotifyListener>> entry : getSubscribed().entrySet()) {
            URL url = entry.getKey();
            // 这里没有办法做到按照key来get，所以使用了这种方式？效率不高
            if (!UrlUtils.isMatch(url, urls.get(0))) {
                continue;
            }
            Set<NotifyListener> listeners = entry.getValue();
            if (CollectionUtils.isEmpty(listeners)) {
                continue;
            }
            for (NotifyListener listener : listeners) {
                try {
                    notify(url, listener, filterEmpty(url, urls));
                } catch (Throwable t) {
                    LOGGER.error("Failed to notify registry event, urls: " + urls + ", cause:" + t.getMessage(), t);
                }
            }
        }
    }


    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        if (CollectionUtils.isEmpty(urls)
                && !Constants.ANY_VALUE.equals(url.getServiceInterface())) {
            LOGGER.warn("Ignore empty notify urls for subscribe url " + url);
            return;
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Notify urls for subscribe url " + url + ", urls: " + urls);
        }
        Map<String, List<URL>> result = new HashMap<>();
        for (URL u : urls) {
            if (UrlUtils.isMatch(u, url)) {
                // 默认是providers的category
                String category = u.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
                List<URL> categoryUrls = result.get(category);
                if (categoryUrls == null) {
                    categoryUrls = new ArrayList<>();
                    result.put(category, categoryUrls);
                }
                categoryUrls.add(u);
            }
        }
        if (result.size() == 0) {
            return;
        }
        Map<String, List<URL>> categoryNotified = getNotified().get(url);
        if (categoryNotified == null) {
            getNotified().putIfAbsent(url, new ConcurrentHashMap<>());
            categoryNotified = getNotified().get(url);
        }

        for (Map.Entry<String, List<URL>> entry : result.entrySet()) {
            String category = entry.getKey();
            List<URL> categoryList = entry.getValue();
            categoryNotified.put(category, categoryList);
            saveProperties(url);
            listener.notify(categoryList);
        }
    }

    private void saveProperties(URL url) {
        if (file == null) {
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            Map<String, List<URL>> categoryNotified = getNotified().get(url);
            if (categoryNotified != null) {
                for (List<URL> us : categoryNotified.values()) {
                    for (URL u : us) {
                        if (sb.length() > 0) {
                            sb.append(URL_SEPARATOR);
                        }
                        sb.append(u.toFullString());
                    }
                }
            }
            String serviceKey = url.getServiceKey();
            if (StringUtils.isNotEmpty(serviceKey)) {
                properties.put(serviceKey, sb.toString());
            }
            long version = lastCacheChanged.incrementAndGet();
            if (syncSaveFile) {
                doSaveProperties(version);
            } else {
                registryCacheExecutor.execute(new SaveProperties(version));
            }
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
        }
    }


    @Override
    public void destroy() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Destroy registry:" + getUrl());
        }

        Set<URL> destroyRegistered = new HashSet<>(getRegistered());
        if (CollectionUtils.isNotEmpty(destroyRegistered)) {
            for (URL url : destroyRegistered) {
                try {
                    unregister(url);
                } catch (Throwable e) {
                    LOGGER.warn("Failed to unregister url "
                            + url + " to registry " + getUrl() +
                            " on destroy, cause: " + e.getMessage(), e);
                }
            }
        }

        Map<URL, Set<NotifyListener>> destroySubscribed = new HashMap<>(getSubscribed());
        if (CollectionUtils.isNotEmpty(destroySubscribed)) {
            for (Map.Entry<URL, Set<NotifyListener>> entry : destroySubscribed.entrySet()) {
                URL url = entry.getKey();
                for (NotifyListener notifyListener : entry.getValue()) {
                    try {
                        unsubscribe(url, notifyListener);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Destroy unsubscribe url " + url);
                        }
                    } catch (Throwable t) {
                        LOGGER.warn("Failed to unsubscribe url " + url + " to registry " + getUrl() + " on destroy, cause: " + t.getMessage(), t);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return getUrl().toString();
    }

    private class SaveProperties implements Runnable {
        private long version;

        private SaveProperties(long version) {
            this.version = version;
        }

        @Override
        public void run() {
            doSaveProperties(version);
        }
    }
}
