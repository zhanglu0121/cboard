package org.cboard.util;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class IPUtil {

    /**
     * 客户端真实IP地址的方法一：
     */
    public static String getRemortIP(HttpServletRequest request) {
        if (request.getHeader("x-forwarded-for") == null) {
            return request.getRemoteAddr();
        }
        return request.getHeader("x-forwarded-for");
    }

    /**
     * 客户端真实IP地址的方法二：
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = "";
        if (request != null) {
            ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        }
        return ip;
    }

    /**
     * 通过访问的Ip地址得到mac地址
     * * @param ip
     * * @return mac
     */
    public String getMacByIp(String ip) {
        String macAddress = "";
        try {
            java.lang.Process process = Runtime.getRuntime().exec("nbtstat -A " + ip);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String str = "";
            while ((str = input.readLine()) != null) {
                str = str.toUpperCase();
                if (str.indexOf("MAC ADDRESS") > 1) {
                    int start = str.indexOf("=");
                    macAddress = str.substring(start + 1, str.length()).trim();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return macAddress;
    }

}
