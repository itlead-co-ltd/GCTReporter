package com.gct.reportgenerator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码请求DTO
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改密码请求对象")
public class ChangePasswordRequest {

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 旧密码
     */
    @Schema(description = "旧密码", example = "admin123", required = true)
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @Schema(description = "新密码", example = "newpassword123", required = true)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "新密码长度必须在6-50个字符之间")
    private String newPassword;

    /**
     * 确认新密码
     */
    @Schema(description = "确认新密码", example = "newpassword123", required = true)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
