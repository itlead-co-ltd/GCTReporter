package com.gct.reportgenerator.interceptor;

import com.gct.reportgenerator.dto.UserSession;
import com.gct.reportgenerator.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 * 拦截需要登录的请求，验证用户会话
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.debug("拦截请求: {} {}", request.getMethod(), requestURI);

        // 检查用户是否已登录
        UserSession userSession = sessionService.getSession(request);
        if (userSession == null) {
            log.warn("未登录用户访问受保护资源: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"未登录或登录已过期\"}");
            return false;
        }

        log.debug("用户已登录: userId={}, username={}", userSession.getUserId(), userSession.getUsername());
        return true;
    }
}
