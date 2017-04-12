package org.jflame.toolkit.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.util.ValidatorHelper;

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
    public static List<InetAddress> getAllAddress() {
        List<InetAddress> ips = new ArrayList<>(2);
        try {
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
                    if (!addr.isLoopbackAddress()) {
                        ips.add(addr);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return ips;
    }

    /**
     * 获取本机局域网ipv4地址
     * 
     * @return InetAddress
     * @throws SocketException
     */
    public static InetAddress localIP4Address() {
        List<InetAddress> allIps = getAllAddress();
        for (InetAddress inetAddress : allIps) {
            if (inetAddress.isSiteLocalAddress() && inetAddress instanceof Inet4Address) {
                return inetAddress;
            }
        }
        return null;
    }

    /**
     * 获取本机局域网ipv4地址字符串
     * 
     * @return ipv4地址
     */
    public static String localIP4Str() {
        return toString(localIP4Address());
    }

    /**
     * 返回本机所有ip的字符串表示形式
     * 
     * @return 本机所有ip字符串
     */
    public static String[] getAllAddressStr() {
        List<InetAddress> ips = getAllAddress();
        String[] ipStrArray = null;
        if (ips != null) {
            ipStrArray = new String[ips.size()];
            for (int i = 0; i < ipStrArray.length; i++) {
                ipStrArray[i] = toString(ips.get(i));
            }
        }
        return ipStrArray;
    }

    /**
     * 返回指定ip地址的字符串表示形式
     * 
     * @param ipAddress
     * @return ip地址字符串
     */
    public static String toString(InetAddress ipAddress) {
        if (ipAddress != null) {
            return StringUtils.substringAfter(ipAddress.toString(), "/");
        }
        return "";
    }

    /**
     * ip bytes 转换为 ip字符串
     * 
     * @param ipBytes
     * @return
     * @throws UnknownHostException
     */
    public static String bytesToIpstr(byte[] ipBytes) {
        InetAddress ip;
        try {
            ip = InetAddress.getByAddress(ipBytes);
        } catch (UnknownHostException e) {
            throw new ConvertException(e);
        }
        return toString(ip);
    }

    /**
     * 从ip的字符串形式得到字节数组形式.
     * 
     * @param ipStr 字符串形式的ip
     * @return 字节数组形式的ip
     */
    public static byte[] ipstrToBytes(String ipStr) {
        if (isIP(ipStr)) {
            InetAddress ip;
            try {
                ip = InetAddress.getByName(ipStr);
            } catch (UnknownHostException e) {
                throw new ConvertException(e);
            }
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
        if (StringUtils.isEmpty(ipv4) || ipv4.length() < 7 || ipv4.length() > 15) {
            return false;
        }
        return ValidatorHelper.regex(ipv4, REGEX_IPV4);
    }

    /**
     * 判断是否是ip v6 字符串
     * 
     * @param ipv6
     * @return
     */
    public static boolean isIPv6(String ipv6) {
        if (StringUtils.isEmpty(ipv6)) {
            return false;
        }
        return ValidatorHelper.regex(ipv6, REGEX_IPV6_STD) || ValidatorHelper.regex(ipv6, REGEX_IPV6_HEX_COMPRESSED);
    }

    /**
     * 判断字符串是否是个ip地址，支持ipv6 or ipv4
     * 
     * @param ip
     * @return
     */
    public static boolean isIP(String ip) {
        return isIPv4(ip) || isIPv6(ip);
    }

    /*
     * public static void main(String[] args) throws SocketException { InetAddress op = localIP4Address();
     * System.out.println(localIP4Str()); System.out.println(localIP4Address().toString()); }
     */
}
