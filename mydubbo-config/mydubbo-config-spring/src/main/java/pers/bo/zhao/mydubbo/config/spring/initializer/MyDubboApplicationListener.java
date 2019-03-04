package pers.bo.zhao.mydubbo.config.spring.initializer;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import pers.bo.zhao.mydubbo.bootstrap.MyDubboBootstrap;

/**
 * @author Bo.Zhao
 * @since 19/3/3
 */
public class MyDubboApplicationListener implements ApplicationListener<ApplicationEvent> {

    private MyDubboBootstrap myDubboBootstrap;

    public MyDubboApplicationListener() {
        this.myDubboBootstrap = new MyDubboBootstrap(false);
    }

    public MyDubboApplicationListener(MyDubboBootstrap myDubboBootstrap) {
        this.myDubboBootstrap = myDubboBootstrap;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            myDubboBootstrap.start();
        } else if (applicationEvent instanceof ContextClosedEvent) {
            myDubboBootstrap.stop();
        }
    }
}
