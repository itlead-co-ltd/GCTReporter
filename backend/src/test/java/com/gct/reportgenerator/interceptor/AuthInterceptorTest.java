package com.gct.reportgenerator.interceptor;

import com.gct.reportgenerator.dto.UserSession;
import com.gct.reportgenerator.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthInterceptor单元测试
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证拦截器单元测试")
class AuthInterceptorTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    private UserSession testUserSession;

    @BeforeEach
    void setUp() {
        testUserSession = UserSession.builder()
                .userId(1L)
                .username("admin")
                .role("ADMIN")
                .build();
    }

    @Test
    @DisplayName("拦截器 - 用户已登录，放行请求")
    void preHandle_UserLoggedIn_ReturnsTrue() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/reports");
        when(request.getMethod()).thenReturn("GET");
        when(sessionService.getSession(request)).thenReturn(testUserSession);

        // When
        boolean result = authInterceptor.preHandle(request, response, new Object());

        // Then
        assertTrue(result);
        verify(sessionService).getSession(request);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("拦截器 - 用户未登录，返回401")
    void preHandle_UserNotLoggedIn_Returns401() throws Exception {
        // Given
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        
        when(request.getRequestURI()).thenReturn("/api/v1/reports");
        when(request.getMethod()).thenReturn("GET");
        when(sessionService.getSession(request)).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        // When
        boolean result = authInterceptor.preHandle(request, response, new Object());

        // Then
        assertFalse(result);
        verify(sessionService).getSession(request);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json;charset=UTF-8");
        
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("未登录或登录已过期"));
    }

    @Test
    @DisplayName("拦截器 - Session为null，返回401")
    void preHandle_SessionNull_Returns401() throws Exception {
        // Given
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        
        when(request.getRequestURI()).thenReturn("/api/v1/current");
        when(request.getMethod()).thenReturn("GET");
        when(sessionService.getSession(request)).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        // When
        boolean result = authInterceptor.preHandle(request, response, new Object());

        // Then
        assertFalse(result);
        verify(response).setStatus(401);
    }
}
