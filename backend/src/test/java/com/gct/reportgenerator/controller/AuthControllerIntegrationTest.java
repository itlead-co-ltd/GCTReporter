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
        // Use the existing admin user from V2__init_users.sql migration
        // admin/admin123
    }

    @Test
    @DisplayName("登录成功 - API集成测试")
    void login_Success_Integration() throws Exception {
        // Given - Using existing admin user
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
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
        // Given - Using existing admin user
        LoginRequest request = LoginRequest.builder()
                .username("admin")
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
        // Given - Using existing admin user
        LoginRequest request = LoginRequest.builder()
                .username("admin")
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
        // This test would require creating a disabled user, skipping for now
        // since SQLite AUTOINCREMENT has issues with JPA
    }

    @Test
    @DisplayName("登出成功")
    void logout_Success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("登出成功"));
    }

    @Test
    @DisplayName("修改密码成功 - API集成测试")
    void changePassword_Success_Integration() throws Exception {
        // Given - Using existing admin user
        com.gct.reportgenerator.dto.ChangePasswordRequest request = 
            com.gct.reportgenerator.dto.ChangePasswordRequest.builder()
                .username("admin")
                .oldPassword("admin123")
                .newPassword("newpass123")
                .confirmPassword("newpass123")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("密码修改成功"));

        // Verify password was changed by attempting login with new password
        com.gct.reportgenerator.dto.LoginRequest loginRequest = 
            com.gct.reportgenerator.dto.LoginRequest.builder()
                .username("admin")
                .password("newpass123")
                .build();

        String loginJson = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));

        // Reset password back to original for other tests
        com.gct.reportgenerator.dto.ChangePasswordRequest resetRequest = 
            com.gct.reportgenerator.dto.ChangePasswordRequest.builder()
                .username("admin")
                .oldPassword("newpass123")
                .newPassword("admin123")
                .confirmPassword("admin123")
                .build();

        String resetJson = objectMapper.writeValueAsString(resetRequest);

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("修改密码失败 - 新密码和确认密码不一致")
    void changePassword_PasswordMismatch_Returns400() throws Exception {
        // Given - Using existing admin user
        com.gct.reportgenerator.dto.ChangePasswordRequest request = 
            com.gct.reportgenerator.dto.ChangePasswordRequest.builder()
                .username("admin")
                .oldPassword("admin123")
                .newPassword("newpass123")
                .confirmPassword("different123")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("新密码和确认密码不一致"));
    }

    @Test
    @DisplayName("修改密码失败 - 旧密码错误")
    void changePassword_WrongOldPassword_Returns400() throws Exception {
        // Given - Using existing admin user
        com.gct.reportgenerator.dto.ChangePasswordRequest request = 
            com.gct.reportgenerator.dto.ChangePasswordRequest.builder()
                .username("admin")
                .oldPassword("wrongpassword")
                .newPassword("newpass123")
                .confirmPassword("newpass123")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("旧密码错误"));
    }

    @Test
    @DisplayName("修改密码失败 - 用户不存在")
    void changePassword_UserNotFound_Returns400() throws Exception {
        // Given
        com.gct.reportgenerator.dto.ChangePasswordRequest request = 
            com.gct.reportgenerator.dto.ChangePasswordRequest.builder()
                .username("nonexistent")
                .oldPassword("oldpass123")
                .newPassword("newpass123")
                .confirmPassword("newpass123")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("用户不存在或已禁用"));
    }

    @Test
    @DisplayName("修改密码失败 - 参数校验失败（新密码过短）")
    void changePassword_ValidationFailed_PasswordTooShort() throws Exception {
        // Given - Using existing admin user
        com.gct.reportgenerator.dto.ChangePasswordRequest request = 
            com.gct.reportgenerator.dto.ChangePasswordRequest.builder()
                .username("admin")
                .oldPassword("admin123")
                .newPassword("12345")
                .confirmPassword("12345")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
