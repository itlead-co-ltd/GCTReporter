package com.gct.reportgenerator.controller;

import com.gct.reportgenerator.dto.CheckNameResponse;
import com.gct.reportgenerator.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 报表管理控制器
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "报表管理", description = "报表的CRUD操作和唯一性校验接口")
public class ReportController {

    private final ReportService reportService;

    /**
     * 检查报表名称是否已存在
     * 
     * @param name 报表名称
     * @return 检查结果
     */
    @GetMapping("/check-name")
    @Operation(summary = "检查报表名称是否已存在", description = "用于报表创建时校验名称唯一性")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "检查成功"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    public ResponseEntity<CheckNameResponse> checkNameExists(
            @Parameter(description = "报表名称", required = true)
            @RequestParam String name) {
        
        log.info("检查报表名称唯一性: {}", name);
        
        boolean exists = reportService.checkNameExists(name);
        CheckNameResponse response = CheckNameResponse.builder()
                .exists(exists)
                .build();
        
        return ResponseEntity.ok(response);
    }
}
