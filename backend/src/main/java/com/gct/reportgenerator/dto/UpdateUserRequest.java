package com.gct.reportgenerator.dto;

import com.gct.reportgenerator.entity.User;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户请求
 * 
 * @author GCT Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
    private String password;

    private User.Role role;

    private Boolean enabled;
}
