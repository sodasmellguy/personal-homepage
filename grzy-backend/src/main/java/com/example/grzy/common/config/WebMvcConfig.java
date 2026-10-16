package com.example.grzy.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 — 静态资源映射
 *
 * @author sodasmell
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /uploads/** 映射到本地文件目录
        String location = "file:" + uploadPath + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }

    /**
     * 为 /uploads/** 静态资源统一附加安全响应头（ResourceHandler 本身无法直接配置响应头，
     * 故通过拦截器实现，拦截器对静态资源处理器同样生效）：
     * 1. X-Content-Type-Options: nosniff —— 禁止浏览器嗅探内容类型，
     *    防止上传的文件被嗅探/当作 HTML、脚本内联执行；
     * 2. Content-Security-Policy: sandbox —— 即使文件被浏览器当作文档打开（直接访问 URL），
     *    也只能运行在无脚本、无同源权限的沙箱中，彻底阻断存储型 XSS。
     *    页面内 <img> 标签加载图片不受该响应头影响，正常展示不受干扰。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("Content-Security-Policy", "sandbox");
                return true;
            }
        }).addPathPatterns("/uploads/**");
    }
}
