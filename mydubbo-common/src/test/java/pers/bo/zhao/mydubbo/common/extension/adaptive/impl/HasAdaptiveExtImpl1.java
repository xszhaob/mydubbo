package pers.bo.zhao.mydubbo.common.extension.adaptive.impl;

import pers.bo.zhao.mydubbo.common.URL;
import pers.bo.zhao.mydubbo.common.extension.Adaptive;
import pers.bo.zhao.mydubbo.common.extension.adaptive.HasAdaptiveExt;

/**
 * @author Bo.Zhao
 * @since 19/2/11
 */
@Adaptive
public class HasAdaptiveExtImpl1 implements HasAdaptiveExt {
    @Override
    public String echo(URL url, String s) {
        return this.getClass().getName();
    }
}
