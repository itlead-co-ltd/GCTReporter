package com.gct.reportgenerator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全配置
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Configuration
public class SecurityConfig {

    /**
     * 密码加密器
     * 使用BCrypt算法
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
