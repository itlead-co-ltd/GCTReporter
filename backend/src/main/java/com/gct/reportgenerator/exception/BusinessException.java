package com.gct.reportgenerator.exception;

import lombok.Getter;

/**
 * 业务异常
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
