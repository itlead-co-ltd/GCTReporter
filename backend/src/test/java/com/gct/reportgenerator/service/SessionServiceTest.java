package com.gct.reportgenerator.service;

import com.gct.reportgenerator.dto.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * SessionService单元测试
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Session服务单元测试")
class SessionServiceTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private SessionService sessionService;

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
    @DisplayName("保存Session - 成功")
    void saveSession_Success() {
        // Given
        when(request.getSession(true)).thenReturn(session);
        when(session.getId()).thenReturn("test-session-id");

        // When
        sessionService.saveSession(request, testUserSession);

        // Then
        verify(request).getSession(true);
        verify(session).setAttribute("USER_SESSION", testUserSession);
    }

    @Test
    @DisplayName("获取Session - Session存在")
    void getSession_SessionExists() {
        // Given
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("USER_SESSION")).thenReturn(testUserSession);

        // When
        UserSession result = sessionService.getSession(request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("admin", result.getUsername());
        assertEquals("ADMIN", result.getRole());
        verify(request).getSession(false);
    }

    @Test
    @DisplayName("获取Session - Session不存在")
    void getSession_SessionNotExists() {
        // Given
        when(request.getSession(false)).thenReturn(null);

        // When
        UserSession result = sessionService.getSession(request);

        // Then
        assertNull(result);
        verify(request).getSession(false);
    }

    @Test
    @DisplayName("获取Session - Session存在但无用户信息")
    void getSession_SessionExistsButNoUserInfo() {
        // Given
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("USER_SESSION")).thenReturn(null);

        // When
        UserSession result = sessionService.getSession(request);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("移除Session - Session存在")
    void removeSession_SessionExists() {
        // Given
        when(request.getSession(false)).thenReturn(session);
        when(session.getId()).thenReturn("test-session-id");
        when(session.getAttribute("USER_SESSION")).thenReturn(testUserSession);

        // When
        sessionService.removeSession(request);

        // Then
        verify(request).getSession(false);
        verify(session).invalidate();
    }

    @Test
    @DisplayName("移除Session - Session不存在")
    void removeSession_SessionNotExists() {
        // Given
        when(request.getSession(false)).thenReturn(null);

        // When
        sessionService.removeSession(request);

        // Then
        verify(request).getSession(false);
        verify(session, never()).invalidate();
    }

    @Test
    @DisplayName("检查是否登录 - 已登录")
    void isLoggedIn_True() {
        // Given
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("USER_SESSION")).thenReturn(testUserSession);

        // When
        boolean result = sessionService.isLoggedIn(request);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("检查是否登录 - 未登录")
    void isLoggedIn_False() {
        // Given
        when(request.getSession(false)).thenReturn(null);

        // When
        boolean result = sessionService.isLoggedIn(request);

        // Then
        assertFalse(result);
    }
}
