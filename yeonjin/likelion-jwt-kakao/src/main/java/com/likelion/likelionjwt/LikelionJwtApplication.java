package com.likelion.likelionjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
// SecurityConfig가 있는 패키지(com.likelion.likelionjwt.global.config)를 반드시 스캔하도록 명시
@ComponentScan(basePackages = {
        "com.likelion.likelionjwt",                     // 기존 메인 패키지
        "com.likelion.likelionjwt.global.config"        // SecurityConfig 패키지
})
public class LikelionJwtApplication {
    public static void main(String[] args) {
        SpringApplication.run(LikelionJwtApplication.class, args);
    }
}
