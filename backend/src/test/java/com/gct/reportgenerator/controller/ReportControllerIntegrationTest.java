package com.gct.reportgenerator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gct.reportgenerator.entity.Report;
import com.gct.reportgenerator.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReportController集成测试
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("报表控制器集成测试")
class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 清空测试数据
        reportRepository.deleteAll();
        
        // 使用原生SQL插入测试数据以绕过SQLite JDBC的generated keys问题
        entityManager.createNativeQuery(
            "INSERT INTO reports (id, name, description, sql_content, creator_id, created_at, updated_at) " +
            "VALUES (1, '已存在的报表', '这是一个已存在的报表', 'SELECT * FROM users', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
            .executeUpdate();
        entityManager.flush();
    }

    @Test
    @DisplayName("检查报表名称 - 名称已存在返回exists=true")
    void checkNameExists_NameExists_ReturnsTrue() throws Exception {
        mockMvc.perform(get("/api/v1/reports/check-name")
                .param("name", "已存在的报表"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(true))
            .andDo(print());
    }

    @Test
    @DisplayName("检查报表名称 - 名称不存在返回exists=false")
    void checkNameExists_NameNotExists_ReturnsFalse() throws Exception {
        mockMvc.perform(get("/api/v1/reports/check-name")
                .param("name", "新报表名称"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(false))
            .andDo(print());
    }

    @Test
    @DisplayName("检查报表名称 - 空名称返回exists=false")
    void checkNameExists_EmptyName_ReturnsFalse() throws Exception {
        mockMvc.perform(get("/api/v1/reports/check-name")
                .param("name", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(false))
            .andDo(print());
    }

    @Test
    @DisplayName("检查报表名称 - 大小写敏感")
    void checkNameExists_CaseSensitive() throws Exception {
        // 数据库中是"已存在的报表"，查询"已存在的报表"应该不存在（大小写不同）
        mockMvc.perform(get("/api/v1/reports/check-name")
                .param("name", "已存在的报表ABC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(false))
            .andDo(print());
    }
}
