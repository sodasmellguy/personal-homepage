package com.example.grzy.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 客户端 IP 解析工具
 * 优先取 X-Forwarded-For 末段（本站 Nginx 追加的一跳，防客户端伪造），否则回退 remoteAddr
 *
 * @author sodasmell
 */
public class ClientIpUtils {

    private ClientIpUtils() {
    }

    /**
     * 从当前请求上下文解析客户端 IP（无请求上下文时返回 "unknown"）
     */
    public static String getClientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return resolve(attrs.getRequest());
        }
        return "unknown";
    }

    /**
     * 从指定请求解析客户端 IP
     * 安全说明：X-Forwarded-For 首段由客户端请求自带、可任意伪造，
     * 只有末端一跳是由我们自己的 Nginx 反代追加的可信地址，因此取末位。
     */
    public static String resolve(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 取最后一个条目（由本站反代写入，不可被客户端预注入）
            int commaIndex = ip.lastIndexOf(',');
            return commaIndex > -1 ? ip.substring(commaIndex + 1).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }
}
