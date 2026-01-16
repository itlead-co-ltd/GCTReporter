package com.gct.reportgenerator.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 
 * @author GCT Team
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
    }
}
