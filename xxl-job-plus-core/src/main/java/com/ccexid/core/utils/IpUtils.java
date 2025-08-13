package com.ccexid.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * IP地址工具类
 * 提供本地IP地址获取、IP格式验证等功能
 *
 * @author xuxueli 2016-5-22 11:38:05
 */
public class IpUtils {
    private static final Logger logger = LoggerFactory.getLogger(IpUtils.class);

    // IP地址常量定义
    private static final String ANY_HOST = "0.0.0.0";
    private static final String LOCAL_HOST = "127.0.0.1";
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    // 本地IP地址缓存
    private static volatile InetAddress localAddress;

    /**
     * 将InetAddress转换为有效的IP地址
     *
     * @param address 网络地址对象
     * @return 有效的IP地址，无效则返回null
     */
    private static InetAddress toValidAddress(InetAddress address) {
        if (address instanceof Inet6Address) {
            Inet6Address ipv6Address = (Inet6Address) address;
            if (isPreferIpv6()) {
                return normalizeIpv6Address(ipv6Address);
            }
        }
        return isValidIpv4Address(address) ? address : null;
    }

    /**
     * 判断是否优先使用IPv6地址
     */
    private static boolean isPreferIpv6() {
        return Boolean.getBoolean("java.net.preferIPv6Addresses");
    }

    /**
     * 验证是否为有效的IPv4地址
     *
     * @param address 网络地址对象
     * @return 是否为有效的IPv4地址
     */
    private static boolean isValidIpv4Address(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }
        String hostAddress = address.getHostAddress();
        return hostAddress != null
                && IP_PATTERN.matcher(hostAddress).matches()
                && !ANY_HOST.equals(hostAddress)
                && !LOCAL_HOST.equals(hostAddress);
    }

    /**
     * 标准化IPv6地址，将范围名称转换为范围ID
     *
     * @param address IPv6地址对象
     * @return 标准化后的IPv6地址
     */
    private static InetAddress normalizeIpv6Address(Inet6Address address) {
        String hostAddress = address.getHostAddress();
        int separatorIndex = hostAddress.lastIndexOf('%');

        if (separatorIndex > 0) {
            try {
                return InetAddress.getByName(
                        hostAddress.substring(0, separatorIndex) + '%' + address.getScopeId()
                );
            } catch (UnknownHostException e) {
                logger.debug("无效的IPv6地址", e);
            }
        }
        return address;
    }

    /**
     * 获取本地IP地址的实际实现
     */
    private static InetAddress getLocalAddressInternal() {
        // 尝试通过本地主机名获取
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            InetAddress validAddress = toValidAddress(localHost);
            if (validAddress != null) {
                return validAddress;
            }
        } catch (Throwable e) {
            logger.error("通过主机名获取本地地址失败", e);
        }

        // 遍历网络接口获取
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            if (networkInterfaces == null) {
                return null;
            }

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                // 过滤无效网络接口
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    InetAddress validAddress = toValidAddress(address);
                    if (validAddress != null) {
                        // 验证地址是否可达
                        try {
                            if (validAddress.isReachable(100)) {
                                return validAddress;
                            }
                        } catch (IOException e) {
                            // 忽略可达性检查失败的情况
                        }
                    }
                }
            }
        } catch (Throwable e) {
            logger.error("遍历网络接口获取本地地址失败", e);
        }

        return null;
    }

    /**
     * 获取本地网络接口的第一个有效IP地址
     *
     * @return 本地IP地址
     */
    public static InetAddress getLocalAddress() {
        if (localAddress != null) {
            return localAddress;
        }
        localAddress = getLocalAddressInternal();
        return localAddress;
    }

    /**
     * 获取IP地址字符串
     *
     * @return IP地址字符串
     */
    public static String getIp() {
        return getLocalAddress().getHostAddress();
    }

    /**
     * 获取IP:端口字符串
     *
     * @param port 端口号
     * @return IP:端口字符串
     */
    public static String getIpPort(int port) {
        String ip = getIp();
        return getIpPort(ip, port);
    }

    /**
     * 构建IP:端口字符串
     *
     * @param ip   IP地址
     * @param port 端口号
     * @return IP:端口字符串
     */
    public static String getIpPort(String ip, int port) {
        if (ip == null) {
            return null;
        }
        return ip + ":" + port;
    }

    /**
     * 解析IP:端口字符串为IP和端口
     *
     * @param address IP:端口字符串
     * @return 包含IP和端口的数组，[0]为IP，[1]为端口
     */
    public static Object[] parseIpPort(String address) {
        String[] parts = address.split(":");
        return new Object[]{parts[0], Integer.parseInt(parts[1])};
    }
}
