package com.gct.reportgenerator.service;

import com.gct.reportgenerator.entity.Report;
import com.gct.reportgenerator.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ReportService单元测试
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("报表服务单元测试")
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    private Report testReport;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testReport = Report.builder()
                .id(1L)
                .name("测试报表")
                .description("这是一个测试报表")
                .sqlContent("SELECT * FROM users")
                .creatorId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("检查报表名称是否存在 - 名称已存在")
    void checkNameExists_NameExists_ReturnsTrue() {
        // Given
        when(reportRepository.existsByName("测试报表")).thenReturn(true);

        // When
        boolean result = reportService.checkNameExists("测试报表");

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("检查报表名称是否存在 - 名称不存在")
    void checkNameExists_NameNotExists_ReturnsFalse() {
        // Given
        when(reportRepository.existsByName("新报表")).thenReturn(false);

        // When
        boolean result = reportService.checkNameExists("新报表");

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("检查报表名称是否存在 - 空名称返回false")
    void checkNameExists_EmptyName_ReturnsFalse() {
        // When
        boolean result = reportService.checkNameExists("");

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("检查报表名称是否存在 - null名称返回false")
    void checkNameExists_NullName_ReturnsFalse() {
        // When
        boolean result = reportService.checkNameExists(null);

        // Then
        assertFalse(result);
    }
}
