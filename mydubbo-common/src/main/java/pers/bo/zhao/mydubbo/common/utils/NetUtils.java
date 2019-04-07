package pers.bo.zhao.mydubbo.common.utils;

import pers.bo.zhao.mydubbo.common.logger.Logger;
import pers.bo.zhao.mydubbo.common.logger.LoggerFactory;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class NetUtils {

    public static final String LOCAL_HOST = "127.0.0.1";
    public static final String ANY_HOST = "0.0.0.0";

    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    private static final Logger LOGGER = LoggerFactory.getLogger(NetUtils.class);

    private static volatile InetAddress LOCAL_ADDRESS = null;

    public static String getLocalHost() {
        InetAddress inetAddress = getLocalAddress();
        return inetAddress == null ? LOCAL_HOST : inetAddress.getHostAddress();
    }

    public static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }
        InetAddress inetAddress = getLocalAddress0();
        LOCAL_ADDRESS = inetAddress;
        return inetAddress;
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (localAddress instanceof Inet6Address) {
                Inet6Address address = (Inet6Address) localAddress;
                // ipv6是否可用
                if (isValid6Address(address)) {
                    // 使ipv6地址标准化
                    return normalizeV6Address(address);
                }
            } else if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable t) {
            LOGGER.warn(t);
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (null == interfaces) {
                return localAddress;
            }
            while (interfaces.hasMoreElements()) {
                try {
                    NetworkInterface network = interfaces.nextElement();
                    Enumeration<InetAddress> addresses = network.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        try {
                            InetAddress address = addresses.nextElement();
                            if (address instanceof Inet6Address) {
                                Inet6Address v6Address = (Inet6Address) address;
                                if (isValidV6Address(v6Address)){
                                    return normalizeV6Address(v6Address);
                                }
                            } else if (isValidAddress(address)) {
                                return address;
                            }
                        } catch (Throwable e) {
                            LOGGER.warn(e);
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.warn(e);
                }
            }
        } catch (Throwable e) {
            LOGGER.warn(e);
        }
        return localAddress;
    }

    static boolean isValidV6Address(Inet6Address address) {
        boolean preferIpv6 = Boolean.getBoolean("java.net.preferIPv6Addresses");
        if (!preferIpv6) {
            return false;
        }
        try {
            return address.isReachable(100);
        } catch (IOException e) {
            // ignore
        }
        return false;
    }

    private static boolean isValidAddress(InetAddress inetAddress) {
        if (inetAddress == null || inetAddress.isLoopbackAddress()) {
            return false;
        }
        String hostAddress = inetAddress.getHostAddress();
        return hostAddress != null
                && !ANY_HOST.equals(hostAddress)
                && !LOCAL_HOST.equals(hostAddress)
                && IP_PATTERN.matcher(hostAddress).matches();

    }

    private static InetAddress normalizeV6Address(Inet6Address address) {
        String hostAddress = address.getHostAddress();
        int i = hostAddress.indexOf('%');
        if (i > 0) {
            try {
                return InetAddress.getByName(hostAddress.substring(0, i) + "%" + address.getScopeId());
            } catch (UnknownHostException e) {
                LOGGER.debug("unknown IPV6 address:", e);
            }
        }
        return address;
    }

    private static boolean isValid6Address(Inet6Address address) {
        boolean preferIPv6 = Boolean.getBoolean("java.net.preferIPv6Addresses");
        if (!preferIPv6) {
            return false;
        }
        try {
            return address.isReachable(100);
        } catch (IOException ignore) {

        }
        return false;
    }

    public static String getIpByHost(String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException e) {
            return hostName;
        }
    }
}
