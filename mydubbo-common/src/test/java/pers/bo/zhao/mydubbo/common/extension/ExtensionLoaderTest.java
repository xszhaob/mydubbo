package pers.bo.zhao.mydubbo.common.extension;

import org.junit.Assert;
import org.junit.Test;
import pers.bo.zhao.mydubbo.common.extension.adaptive.HasAdaptiveExt;
import pers.bo.zhao.mydubbo.common.extension.adaptive.impl.HasAdaptiveExtImpl1;

/**
 * @author Bo.Zhao
 * @since 19/2/11
 */
public class ExtensionLoaderTest {

    @Test
    public void test() {
        HasAdaptiveExt ext = ExtensionLoader.getExtensionLoader(HasAdaptiveExt.class).getAdaptiveExtension();
        Assert.assertEquals(ext.getClass(), HasAdaptiveExtImpl1.class);
    }
}
