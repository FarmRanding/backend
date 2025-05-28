package org.fr.farmranding.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 팜랜딩 API 통일 응답 형식
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "팜랜딩 API 응답 형식")
public class FarmrandingResponseBody<T> {
    
    @Schema(description = "성공 여부", example = "true")
    private Boolean success;
    
    @Schema(description = "응답 코드", example = "100")
    private Integer code;
    
    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;
    
    @Schema(description = "응답 데이터")
    private T data;
    
    @Schema(description = "응답 시간")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * 성공 응답 생성 (데이터 포함)
     */
    public static <T> FarmrandingResponseBody<T> success(T data) {
        return FarmrandingResponseBody.<T>builder()
                .success(true)
                .code(100)
                .message("요청이 성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }
    
    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static <T> FarmrandingResponseBody<T> success() {
        return FarmrandingResponseBody.<T>builder()
                .success(true)
                .code(100)
                .message("요청이 성공적으로 처리되었습니다.")
                .build();
    }
    
    /**
     * 실패 응답 생성
     */
    public static <T> FarmrandingResponseBody<T> error(Integer code, String message) {
        return FarmrandingResponseBody.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }
} 