package pers.bo.zhao.mydubbo.registry;

import pers.bo.zhao.mydubbo.common.URL;

import java.util.List;

public interface NotifyListener {

    void notify(List<URL> urls);
}
