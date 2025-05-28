package org.fr.farmranding.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.common.code.FarmrandingResponseCode;

/**
 * 팜랜딩 API 통일 응답 형식
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "공통 응답 형식")
public record FarmrandingResponseBody<T>(
        @Schema(description = "응답 코드", example = "FR000")
        String code,
        
        @Schema(description = "응답 메시지", example = "성공")
        String message,
        
        @Schema(description = "응답 데이터")
        T data
) {
    public static <T> FarmrandingResponseBody<T> of(FarmrandingResponseCode responseCode, T data) {
        return new FarmrandingResponseBody<>(
                responseCode.getCode(),
                responseCode.getMessage(),
                data
        );
    }
    
    public static <T> FarmrandingResponseBody<T> of(FarmrandingResponseCode responseCode) {
        return of(responseCode, null);
    }
    
    public static <T> FarmrandingResponseBody<T> success(T data) {
        return of(FarmrandingResponseCode.SUCCESS, data);
    }
    
    public static <T> FarmrandingResponseBody<T> success() {
        return of(FarmrandingResponseCode.SUCCESS);
    }
    
    /**
     * 실패 응답 생성
     */
    public static <T> FarmrandingResponseBody<T> error(String code, String message) {
        return new FarmrandingResponseBody<>(
                code,
                message,
                null
        );
    }
} 