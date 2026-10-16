package com.example.grzy.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API 文档配置
 *
 * @author sodasmell
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("personal-homepage个人主页 API")
                        .description("personal-homepage 个人主页后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("赵先生")
                                .email("youxiangtuisongzhnaghao@qq.com")))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                .schemaRequirement("Authorization", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization"));
    }
}
