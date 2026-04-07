package com.ich;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ich.mapper")
public class IchBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(IchBackendApplication.class, args);
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("   非遗云境后端服务启动成功！");
        System.out.println("   API地址: http://localhost:8080/api");
        System.out.println("═══════════════════════════════════════════════════════");
    }
}