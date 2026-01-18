package com.gct.reportgenerator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gct.reportgenerator.dto.LoginRequest;
import com.gct.reportgenerator.entity.User;
import com.gct.reportgenerator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController集成测试
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("认证控制器集成测试")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // 清空数据库
        userRepository.deleteAll();

        // 创建测试用户
        User testUser = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("testpass123"))
                .role(User.UserRole.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("登录成功 - API集成测试")
    void login_Success_Integration() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("testpass123")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.token").value(startsWith("TOKEN_")));
    }

    @Test
    @DisplayName("登录失败 - 用户名不存在")
    void login_UserNotFound_Returns400() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent")
                .password("password")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_WrongPassword_Returns400() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("登录失败 - 参数校验失败（用户名为空）")
    void login_ValidationFailed_UsernameEmpty() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("")
                .password("password")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("登录失败 - 参数校验失败（密码为空）")
    void login_ValidationFailed_PasswordEmpty() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("登录失败 - 用户已禁用")
    void login_UserDisabled_Returns400() throws Exception {
        // Given - 创建已禁用的用户
        User disabledUser = User.builder()
                .username("disabled")
                .password(passwordEncoder.encode("password"))
                .role(User.UserRole.VIEWER)
                .enabled(false)
                .build();
        userRepository.save(disabledUser);

        LoginRequest request = LoginRequest.builder()
                .username("disabled")
                .password("password")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("登出成功")
    void logout_Success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("登出成功"));
    }
}
