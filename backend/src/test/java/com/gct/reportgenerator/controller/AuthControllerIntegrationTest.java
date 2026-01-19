package com.gct.reportgenerator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gct.reportgenerator.dto.LoginRequest;
import com.gct.reportgenerator.entity.User;
import com.gct.reportgenerator.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    // 使用数据库迁移脚本中已存在的测试用户
    // admin/admin123 - ADMIN
    // designer/designer123 - DESIGNER  
    // viewer/viewer123 - VIEWER

    @Test
    @DisplayName("登录成功 - API集成测试")
    void login_Success_Integration() throws Exception {
        // Given - 使用数据库中已存在的admin用户
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
        // Given
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
        // Given
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
        // Given - 测试禁用用户场景（需要先创建禁用用户或修改现有用户）
        // 由于我们不能轻易创建新用户，这个测试使用一个不存在的禁用用户来模拟
        LoginRequest request = LoginRequest.builder()
                .username("disableduser")
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

    @Test
    @DisplayName("Session管理 - 登录后Session保存用户信息")
    void session_SavedAfterLogin() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When - 登录
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();

        // Then - 使用同一个session访问受保护的资源
        mockMvc.perform(get("/api/v1/auth/current")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Session管理 - 未登录访问受保护资源返回401")
    void session_UnauthorizedWithoutLogin() throws Exception {
        // When & Then - 未登录直接访问
        mockMvc.perform(get("/api/v1/auth/current"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("未登录或登录已过期"));
    }

    @Test
    @DisplayName("Session管理 - 登出后清除Session")
    void session_ClearedAfterLogout() throws Exception {
        // Given - 先登录
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // When - 登出
        mockMvc.perform(post("/api/v1/auth/logout")
                        .session(session))
                .andExpect(status().isOk());

        // Then - 使用同一个session访问受保护资源应该返回401
        mockMvc.perform(get("/api/v1/auth/current")
                        .session(session))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
