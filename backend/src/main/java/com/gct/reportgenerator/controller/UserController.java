package com.gct.reportgenerator.controller;

import com.gct.reportgenerator.dto.ApiResponse;
import com.gct.reportgenerator.dto.CreateUserRequest;
import com.gct.reportgenerator.dto.UpdateUserRequest;
import com.gct.reportgenerator.dto.UserDTO;
import com.gct.reportgenerator.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * 
 * @author GCT Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取所有用户
     * 
     * @return 用户列表
     */
    @GetMapping
    public ApiResponse<List<UserDTO>> getAllUsers() {
        log.info("接收到获取所有用户请求");
        List<UserDTO> users = userService.getAllUsers();
        return ApiResponse.success(users);
    }

    /**
     * 根据ID获取用户
     * 
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getUserById(@PathVariable Long id) {
        log.info("接收到获取用户详情请求, ID: {}", id);
        UserDTO user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    /**
     * 创建用户
     * 
     * @param request 创建用户请求
     * @return 创建的用户
     */
    @PostMapping
    public ApiResponse<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("接收到创建用户请求, username: {}", request.getUsername());
        UserDTO user = userService.createUser(request);
        return ApiResponse.success("创建用户成功", user);
    }

    /**
     * 更新用户
     * 
     * @param id 用户ID
     * @param request 更新用户请求
     * @return 更新后的用户
     */
    @PutMapping("/{id}")
    public ApiResponse<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("接收到更新用户请求, ID: {}", id);
        UserDTO user = userService.updateUser(id, request);
        return ApiResponse.success("更新用户成功", user);
    }

    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return 成功消息
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        log.info("接收到删除用户请求, ID: {}", id);
        userService.deleteUser(id);
        return ApiResponse.success("删除用户成功", null);
    }

    /**
     * 启用/禁用用户
     * 
     * @param id 用户ID
     * @param enabled 是否启用
     * @return 更新后的用户
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<UserDTO> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam Boolean enabled) {
        log.info("接收到切换用户状态请求, ID: {}, enabled: {}", id, enabled);
        UserDTO user = userService.toggleUserStatus(id, enabled);
        return ApiResponse.success("切换用户状态成功", user);
    }
}
