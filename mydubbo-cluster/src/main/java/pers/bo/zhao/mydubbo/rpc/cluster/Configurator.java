package pers.bo.zhao.mydubbo.rpc.cluster;

import pers.bo.zhao.mydubbo.common.URL;

/**
 * Configurator. (SPI, Prototype, ThreadSafe)
 *
 */
public interface Configurator extends Comparable<Configurator> {

    /**
     * get the configurator url.
     *
     * @return configurator url.
     */
    URL getUrl();

    /**
     * Configure the provider url.
     * O
     *
     * @param url - old rovider url.
     * @return new provider url.
     */
    URL configure(URL url);

}
