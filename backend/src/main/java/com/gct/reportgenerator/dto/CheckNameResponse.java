package com.gct.reportgenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报表名称唯一性检查响应DTO
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckNameResponse {

    /**
     * 名称是否已存在
     */
    private Boolean exists;
}
