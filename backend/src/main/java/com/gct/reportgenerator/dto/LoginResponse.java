package com.gct.reportgenerator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应对象")
public class LoginResponse {

    /**
     * JWT Token
     */
    @Schema(description = "访问令牌", example = "TOKEN_1_1737878400000")
    private String token;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 用户角色
     */
    @Schema(description = "用户角色", example = "ADMIN", allowableValues = {"ADMIN", "DESIGNER", "VIEWER"})
    private String role;

    /**
     * 用户ID
     */
    @Schema(description = "用户唯一标识", example = "1")
    private Long userId;
}
