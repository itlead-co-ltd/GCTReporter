package com.gct.reportgenerator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全配置类
 * 
 * @author GCT Team
 * @since 1.0.0
 */
@Configuration
public class SecurityConfig {

    /**
     * 密码加密器Bean
     * 使用BCrypt算法加密密码
     * 
     * @return BCrypt密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
