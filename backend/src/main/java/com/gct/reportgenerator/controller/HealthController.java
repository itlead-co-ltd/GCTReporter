package com.gct.reportgenerator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author GCT Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    /**
     * 健康检查接口
     * 
     * @return 健康状态信息
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "GCT Report Generator is running");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "0.0.1-SNAPSHOT");
        return response;
    }

    /**
     * 根路径接口
     * 
     * @return 欢迎信息
     */
    @GetMapping("/")
    public Map<String, String> welcome() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to GCT Report Generator API");
        response.put("docs", "/api/v1/health");
        return response;
    }
}
