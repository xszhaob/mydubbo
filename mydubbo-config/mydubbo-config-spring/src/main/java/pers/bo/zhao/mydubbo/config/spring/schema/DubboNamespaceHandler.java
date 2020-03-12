package pers.bo.zhao.mydubbo.config.spring.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import pers.bo.zhao.mydubbo.common.Version;
import pers.bo.zhao.mydubbo.config.ApplicationConfig;
import pers.bo.zhao.mydubbo.config.ProtocolConfig;
import pers.bo.zhao.mydubbo.config.ProviderConfig;
import pers.bo.zhao.mydubbo.config.RegistryConfig;
import pers.bo.zhao.mydubbo.config.spring.ReferenceBean;
import pers.bo.zhao.mydubbo.config.spring.ServiceBean;

/**
 * DubboNamespaceHandler
 *
 * @export
 */
public class DubboNamespaceHandler extends NamespaceHandlerSupport {

    static {
        Version.checkDuplicate(DubboNamespaceHandler.class);
    }

    @Override
    public void init() {
        registerBeanDefinitionParser("application", new DubboBeanDefinitionParser(ApplicationConfig.class, true));
        registerBeanDefinitionParser("registry", new DubboBeanDefinitionParser(RegistryConfig.class, true));
        registerBeanDefinitionParser("provider", new DubboBeanDefinitionParser(ProviderConfig.class, true));
        registerBeanDefinitionParser("protocol", new DubboBeanDefinitionParser(ProtocolConfig.class, true));
        registerBeanDefinitionParser("service", new DubboBeanDefinitionParser(ServiceBean.class, true));
        registerBeanDefinitionParser("reference", new DubboBeanDefinitionParser(ReferenceBean.class, false));
    }

}
