package org.jflame.toolkit.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.toolkit.valid.ValidatorHelper;

/**
 * ip地址工具类.
 * 
 * @author yucan.zhang
 */
public final class IPAddressHelper {

    /**
     * 获取本机所有ip地址，不包含虚拟网卡和回环地址
     * 
     * @return List&lt;InetAddress&gt;
     * @throws SocketException
     */
    public static List<InetAddress> getAllIPAddress() throws SocketException {
        List<InetAddress> ips = new ArrayList<>(2);
        Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        NetworkInterface current = null;
        while (allNetInterfaces.hasMoreElements()) {
            current = allNetInterfaces.nextElement();
            if (!current.isUp() || current.isLoopback() || current.isVirtual()) {
                continue;
            }
            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress() && !addr.isAnyLocalAddress()) {
                    ips.add(addr);
                }
            }
        }
        return ips;
    }

    static volatile String hostIp;

    /**
     * 返回本机ip地址，优先取外网地址，无外网地址返回局域网地址
     * 
     * @return
     */
    public static String getHostIP() {
        InetAddress realIPAddr = null;
        if (hostIp == null) {
            List<InetAddress> allIps;
            try {
                allIps = getAllIPAddress();
                for (InetAddress inetAddress : allIps) {
                    if (!inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    } else {
                        realIPAddr = inetAddress;
                    }
                }
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }
        hostIp = realIPAddr != null ? realIPAddr.getHostAddress() : null;
        return hostIp;
    }

    /**
     * 获取本机局域网ip地址
     * 
     * @return InetAddress
     */
    public static InetAddress getLocalIPAddress() {
        List<InetAddress> allIps;
        try {
            allIps = getAllIPAddress();
            for (InetAddress inetAddress : allIps) {
                if (inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress()) {
                    return inetAddress;
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    static volatile String localIp;

    /**
     * 获取本机局域网ip字符串,无法获取ip返回空字符
     * 
     * @return
     */
    public static String getLocalIP() {
        if (localIp == null) {
            InetAddress addr = getLocalIPAddress();
            localIp = addr != null ? addr.getHostAddress() : "";
        }
        return localIp;
    }

    /**
     * 返回本机所有ip字符串
     * 
     * @return 本机所有ip字符串
     */
    public static String[] getAllIPs() {
        List<InetAddress> ips;
        String[] ipStrArray = null;
        try {
            ips = getAllIPAddress();
            if (ips != null) {
                ipStrArray = new String[ips.size()];
                for (int i = 0; i < ipStrArray.length; i++) {
                    ipStrArray[i] = ips.get(i).getHostAddress();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return ipStrArray;
    }

    /**
     * ip bytes 转换为 ip字符串
     * 
     * @param ipBytes
     * @return
     * @throws UnknownHostException
     */
    public static String fromBytes(byte[] ipBytes) throws UnknownHostException {
        InetAddress ip = InetAddress.getByAddress(ipBytes);
        return ip.getHostAddress();
    }

    /**
     * 从ip的字符串形式得到字节数组形式.
     * 
     * @param ipStr 字符串形式的ip
     * @return 字节数组形式的ip
     * @throws UnknownHostException
     */
    public static byte[] toBytes(String ipStr) throws UnknownHostException {
        if (isIP(ipStr)) {
            InetAddress ip = InetAddress.getByName(ipStr);
            return ip.getAddress();
        } else {
            throw new ConvertException("不正确的ip:" + ipStr);
        }
    }

    /**
     * ipv4正则
     */
    public final static String REGEX_IPV4 = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d"
            + "|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
    /**
     * ipv6 普通格式正则
     */
    public static final String REGEX_IPV6_STD = "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
    /**
     * ipv6压缩格式正则
     */
    public static final String REGEX_IPV6_HEX_COMPRESSED = "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)"
            + "::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$";

    /**
     * 判断是否是ip v4 字符串
     * 
     * @param ipv4
     * @return
     */
    public static boolean isIPv4(String ipv4) {
        if (StringHelper.isEmpty(ipv4)) {
            return false;
        }
        return ValidatorHelper.regex(ipv4.trim(), REGEX_IPV4);
    }

    /**
     * 判断是否是ip v6 字符串
     * 
     * @param ipv6
     * @return
     */
    public static boolean isIPv6(String ipv6) {
        if (StringHelper.isEmpty(ipv6)) {
            return false;
        }
        ipv6 = ipv6.trim();
        return ValidatorHelper.regex(ipv6, REGEX_IPV6_STD) || ValidatorHelper.regex(ipv6, REGEX_IPV6_HEX_COMPRESSED);
    }

    /**
     * 判断字符串是否是个ip地址，支持ipv6 or ipv4
     * 
     * @param ip ip地址字符
     * @return
     */
    public static boolean isIP(String ip) {
        return isIPv4(ip) || isIPv6(ip);
    }

    /**
     * 是否是局域网ip地址，不包括回环ip，广播ip
     * 
     * @param ip ip地址字符
     * @return
     */
    public static boolean isLanIP(String ip) {
        if (StringHelper.isEmpty(ip)) {
            return false;
        }
        try {
            InetAddress ipAddr = InetAddress.getByName(ip);
            return ipAddr.isSiteLocalAddress() && !ipAddr.isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 判断ip是否在指定网段内
     * 
     * @param ip ip地址
     * @param ipSegment 指定网段,格式:192.168.0.0/32
     */
    public static boolean isInRange(String ip, String ipSegment) {
        String[] ips = ip.split("\\.");
        int ipAddr = (Integer.parseInt(ips[0]) << 24) | (Integer.parseInt(ips[1]) << 16)
                | (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
        int type = Integer.parseInt(ipSegment.replaceAll(".*/", ""));
        int mask = 0xFFFFFFFF << (32 - type);
        String cidrIp = ipSegment.replaceAll("/.*", "");
        String[] cidrIps = cidrIp.split("\\.");
        int cidrIpAddr = (Integer.parseInt(cidrIps[0]) << 24) | (Integer.parseInt(cidrIps[1]) << 16)
                | (Integer.parseInt(cidrIps[2]) << 8) | Integer.parseInt(cidrIps[3]);

        return (ipAddr & mask) == (cidrIpAddr & mask);
    }

    /*public static void main(String[] args) {
        System.out.println(getHostIP());
    }*/
}
