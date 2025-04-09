package com.portfolio.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 특정 출처 허용 (allowCredentials = true 일 때는 와일드카드 사용 불가)
        config.setAllowedOrigins(List.of("http://localhost:8080", "http://localhost:3000"));
        // 모든 헤더 허용
        config.addAllowedHeader("*");
        // 모든 HTTP 메서드 허용
        config.addAllowedMethod("*");
        // 인증 정보 허용
        config.setAllowCredentials(true);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
