package org.fr.farmranding.common.exception;

import lombok.Getter;
import org.fr.farmranding.common.code.FarmrandingResponseCode;

/**
 * 팜랜딩 비즈니스 예외 클래스
 */
@Getter
public class BusinessException extends RuntimeException {
    private final FarmrandingResponseCode errorCode;

    public BusinessException(FarmrandingResponseCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(FarmrandingResponseCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(FarmrandingResponseCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
} 