package com.escanor.gateway.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

public abstract class IPUtils {

    static String X_FORWARDED_FOR = "x-forwarded-for";
    static String PROXY_CLIENT_IP = "Proxy-Client-IP";
    static String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    static String X_REAL_IP = "X-Real-IP";
    static String UNKNOWN = "unknown";

    public static String getIp(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders httpHeaders = request.getHeaders();
        String ip = httpHeaders.getFirst(X_FORWARDED_FOR);
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = httpHeaders.getFirst(PROXY_CLIENT_IP);
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = httpHeaders.getFirst(WL_PROXY_CLIENT_IP);
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = httpHeaders.getFirst(X_REAL_IP);
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            if (null != request.getRemoteAddress()) {
                ip = request.getRemoteAddress().getAddress().getHostAddress();
            }
        }
        return ip;
    }

}
