package com.example.grzy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * personal-homepage 应用启动类
 *
 * @author sodasmell
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
@MapperScan("com.example.grzy.mapper")
public class GrzyApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrzyApplication.class, args);
        System.out.println("====================================");
        System.out.println("   personal-homepage 后端启动成功!");
        System.out.println("   API文档: http://localhost:8080/doc.html");
        System.out.println("====================================");
    }
}
