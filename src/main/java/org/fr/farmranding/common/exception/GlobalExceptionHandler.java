package org.fr.farmranding.common.exception;

import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 팜랜딩 전역 예외 처리기
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<FarmrandingResponseBody<Void>> handleBusinessException(BusinessException e) {
        FarmrandingResponseCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(FarmrandingResponseBody.error(errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * Spring Security 인증 예외 처리 (401)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<FarmrandingResponseBody<Void>> handleAuthenticationException(AuthenticationException e) {
        FarmrandingResponseCode errorCode = FarmrandingResponseCode.AUTHENTICATION_FAILED;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(FarmrandingResponseBody.error(errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * Spring Security 권한 예외 처리 (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<FarmrandingResponseBody<Void>> handleAccessDeniedException(AccessDeniedException e) {
        FarmrandingResponseCode errorCode = FarmrandingResponseCode.ACCESS_DENIED;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(FarmrandingResponseBody.error(errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * 컨트롤러 유효성 검증
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FarmrandingResponseBody<Void>> handleValidationException(MethodArgumentNotValidException e) {
        return ResponseEntity
                .status(FarmrandingResponseCode.VALIDATION_ERROR.getHttpStatus())
                .body(FarmrandingResponseBody.error(
                        FarmrandingResponseCode.VALIDATION_ERROR.getCode(),
                        e.getBindingResult().getAllErrors().get(0).getDefaultMessage() // 첫 번째 메시지만 노출
                ));
    }

    /**
     * 요청 형식이 잘못된 경우 처리 (ex. Enum 파싱 실패 등)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<FarmrandingResponseBody<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        FarmrandingResponseCode errorCode = FarmrandingResponseCode.BAD_JSON_FORMAT;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(FarmrandingResponseBody.error(errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * 그 외 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<FarmrandingResponseBody<Void>> handleGeneralException(Exception e) {
        return ResponseEntity
                .status(FarmrandingResponseCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(FarmrandingResponseBody.error(
                        FarmrandingResponseCode.INTERNAL_SERVER_ERROR.getCode(),
                        FarmrandingResponseCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
} 