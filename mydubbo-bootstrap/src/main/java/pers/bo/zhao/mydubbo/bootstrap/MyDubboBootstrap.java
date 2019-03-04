package pers.bo.zhao.mydubbo.bootstrap;

import pers.bo.zhao.mydubbo.config.MyDubboShutdownHook;
import pers.bo.zhao.mydubbo.config.ServiceConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bo.Zhao
 * @since 19/3/3
 */
public class MyDubboBootstrap {

    private List<ServiceConfig> serviceConfigList;


    private final boolean registerShutdownHookOnStart;

    private MyDubboShutdownHook shutdownHook;

    public MyDubboBootstrap() {
        this(false, MyDubboShutdownHook.getDubboShutdownHook());
    }

    public MyDubboBootstrap(boolean registerShutdownHookOnStart) {
        this(registerShutdownHookOnStart, MyDubboShutdownHook.getDubboShutdownHook());
    }

    public MyDubboBootstrap(boolean registerShutdownHookOnStart, MyDubboShutdownHook shutdownHook) {
        this.serviceConfigList = new ArrayList<>(0);
        this.registerShutdownHookOnStart = registerShutdownHookOnStart;
        this.shutdownHook = shutdownHook;
    }

    public void start() {
        if (registerShutdownHookOnStart) {
            registerShutdownHook();
        } else {
            removeShutdownHook();
        }
        for (ServiceConfig serviceConfig : serviceConfigList) {
            // TODO 暴露服务
//            serviceConfig.export();
        }
    }

    public void stop() {
        for (ServiceConfig serviceConfig : serviceConfigList) {
            // TODO 收回服务
//            serviceConfig.unexport();
        }
        // TODO: 19/3/3 在shutdownHook的run方法就调用了destroyAll，为何在此处再次调用？
        shutdownHook.destroyedAll();
        if (registerShutdownHookOnStart) {
            removeShutdownHook();
        }
    }

    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public void removeShutdownHook() {
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }
}
