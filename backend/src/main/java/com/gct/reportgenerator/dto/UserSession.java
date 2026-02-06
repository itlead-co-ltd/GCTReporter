package com.gct.reportgenerator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户会话信息DTO
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户会话信息对象")
public class UserSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

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
}
