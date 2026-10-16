package com.example.grzy.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * 跨域配置
 * 通过环境变量 cors.allowed-origins 控制允许的来源，生产环境应限制为具体域名
 *
 * @author sodasmell
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 从配置中读取允许的来源（逗号分隔），生产环境应配置具体域名
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        boolean wildcard = origins.stream().anyMatch(o -> "*".equals(o.trim()));
        origins.forEach(origin -> config.addAllowedOriginPattern(origin.trim()));
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有请求方法
        config.addAllowedMethod("*");
        // 安全：通配来源时禁止携带凭证，杜绝"任意站点 + 携带 Cookie"组合
        config.setAllowCredentials(!wildcard);
        // 预检请求缓存时间
        config.setMaxAge(3600L);
        // 暴露响应头
        config.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
