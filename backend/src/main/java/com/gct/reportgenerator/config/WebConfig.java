package com.gct.reportgenerator.config;

import com.gct.reportgenerator.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 * 注册拦截器和其他Web相关配置
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有API请求
                .excludePathPatterns(
                        "/api/v1/auth/login",       // 登录接口不拦截
                        "/api/v1/auth/logout",      // 登出接口不拦截
                        "/api/v1/health/**",        // 健康检查接口不拦截
                        "/api-docs/**",             // API文档不拦截
                        "/swagger-ui/**",           // Swagger UI不拦截
                        "/swagger-ui.html",         // Swagger UI不拦截
                        "/error"                    // 错误页面不拦截
                );
    }
}
