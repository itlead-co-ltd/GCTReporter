package com.gct.reportgenerator.controller;

import com.gct.reportgenerator.dto.LoginRequest;
import com.gct.reportgenerator.dto.LoginResponse;
import com.gct.reportgenerator.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "认证管理", description = "用户登录、登出和会话管理相关接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应（包含token）
     */
    @Operation(
        summary = "用户登录", 
        description = "使用用户名和密码进行登录，成功后返回Token用于后续API调用认证"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "登录成功",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "用户名或密码错误"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "用户已被禁用"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @Parameter(description = "登录请求信息", required = true)
        @Valid @RequestBody LoginRequest request
    ) {
        log.info("收到登录请求, username: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 用户登出
     * 
     * @return 登出成功响应
     */
    @Operation(
        summary = "用户登出", 
        description = "用户主动登出，清除会话信息（后续将实现Token黑名单机制）"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "登出成功"
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        log.info("用户登出");
        // 暂时简单处理，前端清除token即可
        // 后续可以加入token黑名单机制
        return ResponseEntity.ok(Map.of("message", "登出成功"));
    }

    /**
     * 获取当前用户信息
     * 
     * @return 用户信息
     */
    @Operation(
        summary = "获取当前用户信息", 
        description = "根据Token获取当前登录用户的详细信息（待实现Token解析逻辑）"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "获取成功"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "未登录或Token已失效"
        )
    })
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        // TODO: 从token中解析用户信息
        // 暂时返回mock数据
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1);
        userInfo.put("username", "admin");
        userInfo.put("role", "ADMIN");
        userInfo.put("enabled", true);
        
        return ResponseEntity.ok(userInfo);
    }
}
