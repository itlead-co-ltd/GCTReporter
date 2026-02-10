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
 * 报表实体类
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Entity
@Table(name = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 报表名称（唯一，最多50字符）
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 报表描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * SQL查询内容
     */
    @Column(name = "sql_content", nullable = false, columnDefinition = "TEXT")
    private String sqlContent;

    /**
     * 创建者ID
     */
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

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
}
