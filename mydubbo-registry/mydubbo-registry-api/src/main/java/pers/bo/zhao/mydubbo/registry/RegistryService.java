package pers.bo.zhao.mydubbo.registry;

import pers.bo.zhao.mydubbo.common.URL;

import java.util.List;

public interface RegistryService {

    void register(URL url);

    void unregister(URL url);

    void subscribe(URL url, NotifyListener listener);

    void unsubscribe(URL url, NotifyListener listener);

    /**
     * 查询注册列表，与订阅的推模式相对应，这里为拉模式，只返回一次结果。
     *
     * @param url 查询条件，不允许为空，如：consumer://10.20.153.10/com.alibaba.foo.BarService?version=1.0.0&application=kylin
     * @return 已注册信息列表，可能为空，含义同{@link NotifyListener#notify(List<URL>)}的参数。
     */
    List<URL> lookup(URL url);
}
