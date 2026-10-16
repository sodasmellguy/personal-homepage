package com.example.grzy.common.config;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * API文档访问鉴权过滤器
 * 1. /doc.html 请求从 URL 参数读取 Token，注入到请求头，并设置 Cookie
 * 2. 子请求（/webjars/... 等）从 Cookie 读取 Token，注入到请求头
 * 供 Sa-Token 拦截器验证登录状态
 *
 * @author sodasmell
 */
@Order(1)
@Component
public class DocAuthFilter implements Filter {

    private static final String TOKEN_PARAM = "token";
    private static final String COOKIE_NAME = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<String> DOC_PATHS = List.of(
            "/doc.html", "/webjars/", "/v3/api-docs/", "/swagger-resources/"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        boolean isDocPath = DOC_PATHS.stream().anyMatch(uri::startsWith);

        if (isDocPath) {
            // 优先从 URL 参数读取 token，其次从 Cookie 读取
            String token = httpRequest.getParameter(TOKEN_PARAM);
            if (token == null || token.isEmpty()) {
                token = getCookieValue(httpRequest, COOKIE_NAME);
            }

            if (token != null && !token.isEmpty()) {
                // /doc.html 首次访问时，将 token 写入 Cookie（供子请求使用）
                if (uri.equals("/doc.html")) {
                    Cookie cookie = new Cookie(COOKIE_NAME, token);
                    cookie.setPath("/");
                    cookie.setHttpOnly(false);
                    cookie.setMaxAge(604800); // 7天
                    httpResponse.addCookie(cookie);
                }

                // 包装请求，注入 Authorization header
                final String authToken = BEARER_PREFIX + token;
                HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(httpRequest) {
                    private final Map<String, String> headerMap = new HashMap<>();

                    {
                        headerMap.put(COOKIE_NAME, authToken);
                    }

                    @Override
                    public String getHeader(String name) {
                        String value = headerMap.get(name);
                        return value != null ? value : super.getHeader(name);
                    }

                    @Override
                    public Enumeration<String> getHeaderNames() {
                        Set<String> names = new HashSet<>(Collections.list(super.getHeaderNames()));
                        names.addAll(headerMap.keySet());
                        return Collections.enumeration(names);
                    }

                    @Override
                    public Enumeration<String> getHeaders(String name) {
                        String value = headerMap.get(name);
                        if (value != null) {
                            return Collections.enumeration(List.of(value));
                        }
                        return super.getHeaders(name);
                    }
                };
                chain.doFilter(wrapper, response);
                return;
            }

            // 未携带 token，返回 401
            httpResponse.setStatus(401);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"code\":401,\"message\":\"请先登录\",\"data\":null,\"success\":false}");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * 从 Cookie 中读取指定名称的值
     */
    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
