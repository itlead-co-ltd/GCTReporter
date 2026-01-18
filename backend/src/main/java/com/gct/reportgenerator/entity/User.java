package com.gct.reportgenerator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名（唯一）
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    @Column(nullable = false, length = 100)
    private String password;

    /**
     * 用户角色
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 用户角色枚举
     */
    public enum UserRole {
        /**
         * 管理员 - 完整权限
         */
        ADMIN,
        
        /**
         * 设计者 - 创建和管理报表
         */
        DESIGNER,
        
        /**
         * 查看者 - 仅查看和导出报表
         */
        VIEWER
    }
}
