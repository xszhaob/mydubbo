package pers.bo.zhao.mydubbo.container.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;
import pers.bo.zhao.mydubbo.common.utils.ConfigUtils;
import pers.bo.zhao.mydubbo.common.utils.StringUtils;
import pers.bo.zhao.mydubbo.config.spring.initializer.MyDubboApplicationListener;
import pers.bo.zhao.mydubbo.container.Container;

/**
 * @author Bo.Zhao
 * @since 19/3/3
 */
public class SpringContainer implements Container {

    private static final String SPRING_CONFIG = "mydubbo.spring.config";
    private static final String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/spring/*.xml";

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringContainer.class);

    static ClassPathXmlApplicationContext context;

    public static ClassPathXmlApplicationContext getContext() {
        return context;
    }

    @Override
    public void start() {
        String configPath = ConfigUtils.getProperty(SPRING_CONFIG);
        if (StringUtils.isEmpty(configPath)) {
            configPath = DEFAULT_SPRING_CONFIG;
        }

        context = new ClassPathXmlApplicationContext(configPath.split("[,\\s]+"), false);
        context.addApplicationListener(new MyDubboApplicationListener());
        context.registerShutdownHook();
        context.refresh();
        context.start();
    }

    @Override
    public void stop() {
        if (context != null) {
            try {
                context.stop();
                context.close();
                context = null;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
