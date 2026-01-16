package com.gct.reportgenerator.controller;

import com.gct.reportgenerator.dto.CreateUserRequest;
import com.gct.reportgenerator.dto.UpdateUserRequest;
import com.gct.reportgenerator.dto.UserDTO;
import com.gct.reportgenerator.entity.User;
import com.gct.reportgenerator.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController集成测试
 * 
 * @author GCT Team
 * @since 1.0.0
 */
@WebMvcTest(UserController.class)
@DisplayName("US005: 用户控制器集成测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("T005-5: 获取所有用户 - API测试")
    void getAllUsers_Success() throws Exception {
        // Given
        List<UserDTO> users = Arrays.asList(
                UserDTO.builder()
                        .id(1L)
                        .username("admin")
                        .role(User.Role.ADMIN)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                UserDTO.builder()
                        .id(2L)
                        .username("designer")
                        .role(User.Role.DESIGNER)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].username").value("admin"))
                .andExpect(jsonPath("$.data[1].username").value("designer"));
    }

    @Test
    @DisplayName("T005-5: 根据ID获取用户 - API测试")
    void getUserById_Success() throws Exception {
        // Given
        UserDTO user = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .role(User.Role.VIEWER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(userService.getUserById(1L)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("T005-5: 创建用户 - API测试")
    void createUser_Success() throws Exception {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .username("newuser")
                .password("password123")
                .role(User.Role.DESIGNER)
                .enabled(true)
                .build();

        UserDTO createdUser = UserDTO.builder()
                .id(1L)
                .username("newuser")
                .role(User.Role.DESIGNER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("创建用户成功"))
                .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    @DisplayName("T005-5: 创建用户 - 参数校验失败")
    void createUser_ValidationFailed() throws Exception {
        // Given - 用户名太短
        CreateUserRequest request = CreateUserRequest.builder()
                .username("ab")
                .password("password123")
                .role(User.Role.DESIGNER)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("T005-5: 更新用户 - API测试")
    void updateUser_Success() throws Exception {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .password("newpassword")
                .role(User.Role.ADMIN)
                .build();

        UserDTO updatedUser = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .role(User.Role.ADMIN)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新用户成功"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("T005-5: 删除用户 - API测试")
    void deleteUser_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除用户成功"));
    }

    @Test
    @DisplayName("T005-5: 启用/禁用用户 - API测试")
    void toggleUserStatus_Success() throws Exception {
        // Given
        UserDTO updatedUser = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .role(User.Role.VIEWER)
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.toggleUserStatus(1L, false)).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(patch("/api/v1/users/1/status")
                        .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("切换用户状态成功"))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }
}
