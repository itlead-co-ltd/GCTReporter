package com.gct.reportgenerator.service;

import com.gct.reportgenerator.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 报表服务类
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;

    /**
     * 检查报表名称是否已存在
     * 
     * @param name 报表名称
     * @return 是否已存在
     */
    public boolean checkNameExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        boolean exists = reportRepository.existsByName(name);
        log.debug("检查报表名称: {}, 是否存在: {}", name, exists);
        return exists;
    }
}
