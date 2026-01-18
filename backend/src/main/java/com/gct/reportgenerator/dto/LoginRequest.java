package com.gct.reportgenerator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求DTO
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录请求对象")
public class LoginRequest {

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", example = "admin123", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
}
