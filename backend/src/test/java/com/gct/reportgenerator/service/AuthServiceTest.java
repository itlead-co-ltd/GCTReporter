package com.gct.reportgenerator.service;

import com.gct.reportgenerator.dto.LoginRequest;
import com.gct.reportgenerator.dto.LoginResponse;
import com.gct.reportgenerator.entity.User;
import com.gct.reportgenerator.exception.BusinessException;
import com.gct.reportgenerator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

/**
 * AuthService单元测试
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务单元测试")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testUser = User.builder()
                .id(1L)
                .username("admin")
                .password("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi") // admin123加密后
                .role(User.UserRole.ADMIN)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        loginRequest = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();
    }

    @Test
    @DisplayName("登录成功 - 正确的用户名和密码")
    void login_Success() {
        // Given
        when(userRepository.findByUsernameAndEnabled("admin", true))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("admin123", testUser.getPassword()))
                .thenReturn(true);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("admin", response.getUsername());
        assertEquals("ADMIN", response.getRole());
        assertEquals(1L, response.getUserId());
        assertNotNull(response.getToken());
        assertTrue(response.getToken().startsWith("TOKEN_"));
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_UserNotFound() {
        // Given
        when(userRepository.findByUsernameAndEnabled(anyString(), anyBoolean()))
                .thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_WrongPassword() {
        // Given
        when(userRepository.findByUsernameAndEnabled("admin", true))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword()))
                .thenReturn(false);

        LoginRequest wrongRequest = LoginRequest.builder()
                .username("admin")
                .password("wrongpassword")
                .build();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(wrongRequest);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 用户已禁用")
    void login_UserDisabled() {
        // Given
        when(userRepository.findByUsernameAndEnabled("admin", true))
                .thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("Token生成测试 - Token格式正确")
    void generateToken_Format() {
        // Given
        when(userRepository.findByUsernameAndEnabled("admin", true))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("admin123", testUser.getPassword()))
                .thenReturn(true);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        String token = response.getToken();
        assertTrue(token.matches("TOKEN_\\d+_\\d+"));
    }

    @Test
    @DisplayName("登出成功 - 记录日志")
    void logout_Success() {
        // When - 执行登出操作不应抛出异常
        assertDoesNotThrow(() -> {
            authService.logout();
        });
        
        // Then - 验证方法能正常执行
        // 注意：实际的Session清除和日志记录由方法内部处理
    }
}
