package com.example.grzy.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.example.grzy.common.exception.BusinessException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Sa-Token 配置
 *
 * @author sodasmell
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 第一层：登录校验
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/front/**",
                        // 图片上传对访客开放（友链申请等场景），服务端有图片类型白名单和大小限制；通用/文件上传仍需登录
                        "/api/upload/image"
                );

        // 第二层：角色校验 —— 管理类接口要求用户至少拥有一个启用角色（普通注册访客无角色，无法访问）。
        // 此前只有登录校验，任意注册用户登录后即可调用全部管理接口（垂直越权）。
        // 具体接口的细粒度权限由各 Controller 上的 @SaCheckPermission 注解控制
        // （SaInterceptor 默认开启注解鉴权），admin 角色在 StpInterfaceImpl 中持有通配权限 "*"。
        registry.addInterceptor(new SaInterceptor(handle -> {
                    List<String> roles = StpUtil.getRoleList();
                    if (roles == null || roles.isEmpty()) {
                        throw new BusinessException("无访问权限，请联系管理员分配角色");
                    }
                }))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/front/**",
                        "/api/auth/**",
                        "/api/upload/image"
                );

        // 拦截 Knife4j 文档路径，要求登录
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-resources/**")
                .excludePathPatterns();
    }
}
