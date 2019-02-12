package pers.bo.zhao.mydubbo.demo.provider;

import pers.bo.zhao.mydubbo.demo.DemoService;

/**
 * @author Bo.Zhao
 * @since 19/1/22
 */
public class DemoServiceImpl implements DemoService {

    @Override
    public String sayHello(String name) {
        return "Hello," + name;
    }
}
