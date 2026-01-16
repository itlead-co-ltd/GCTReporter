package com.gct.reportgenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应封装
 * 
 * @author GCT Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private Integer code;
    private String message;
    private T data;

    /**
     * 成功响应
     * 
     * @param data 响应数据
     * @return API响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .build();
    }

    /**
     * 成功响应（带自定义消息）
     * 
     * @param message 响应消息
     * @param data 响应数据
     * @return API响应
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 错误响应
     * 
     * @param code 错误码
     * @param message 错误消息
     * @return API响应
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
