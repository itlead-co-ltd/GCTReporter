package com.gct.reportgenerator.controller;

import com.gct.reportgenerator.dto.UserDTO;
import com.gct.reportgenerator.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * 
 * 提供用户管理相关的REST API接口
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户的查询和管理接口")
public class UserController {
    
    private final UserService userService;
    
    /**
     * 获取用户列表
     * 
     * @param keyword 搜索关键字（可选）
     * @return 用户列表
     */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "获取所有用户列表，支持按用户名搜索")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<List<UserDTO>> getUsers(
            @Parameter(description = "搜索关键字（用户名）", required = false)
            @RequestParam(required = false) String keyword) {
        
        log.info("获取用户列表，搜索关键字: {}", keyword);
        
        List<UserDTO> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userService.searchUsers(keyword);
        } else {
            users = userService.getAllUsers();
        }
        
        return ResponseEntity.ok(users);
    }
    
    /**
     * 根据ID获取用户详情
     * 
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id) {
        
        log.info("获取用户详情，ID: {}", id);
        
        UserDTO user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(user);
    }
}
