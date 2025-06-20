package org.fr.farmranding.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 팜랜딩 API 응답 코드 정의
 * 
 * 모든 API 응답에서 사용되는 표준화된 응답 코드를 정의합니다.
 * HTTP 상태 코드와 비즈니스 응답 코드를 함께 관리합니다.
 */
@Getter
@RequiredArgsConstructor
public enum FarmrandingResponseCode {
    
    // SUCCESS
    SUCCESS(HttpStatus.OK, "FR000", "성공"),
    
    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "FR001", "사용자를 찾을 수 없습니다"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "FR002", "이미 존재하는 사용자입니다"),
    
    // AUTH
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "FR101", "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "FR102", "만료된 토큰입니다"),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "FR103", "인증에 실패했습니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "FR104", "접근이 거부되었습니다"),
    
    // OAUTH2
    OAUTH2_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "FR201", "OAuth2 인증에 실패했습니다"),
    OAUTH2_USER_INFO_ERROR(HttpStatus.BAD_REQUEST, "FR202", "OAuth2 사용자 정보를 가져올 수 없습니다"),
    
    // VALIDATION
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "FR301", "잘못된 입력값입니다"),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "FR302", "필수 필드가 누락되었습니다"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "FR303", "유효성 검증에 실패했습니다"),
    BAD_JSON_FORMAT(HttpStatus.BAD_REQUEST, "FR304", "잘못된 JSON 형식입니다"),
    
    // MEMBERSHIP & USAGE
    ALREADY_PRO_MEMBERSHIP(HttpStatus.BAD_REQUEST, "FR401", "이미 프로 멤버십 사용자입니다"),
    AI_BRANDING_USAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "FR402", "AI 브랜딩 사용 한도를 초과했습니다"),
    PRICING_USAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "FR403", "가격 제안 사용 한도를 초과했습니다"),
    
    // PRICE QUOTE
    PRICE_QUOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "FR411", "가격 견적 요청을 찾을 수 없습니다"),
    PRICE_QUOTE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FR412", "가격 견적 요청에 대한 접근 권한이 없습니다"),
    PRICE_QUOTE_CANNOT_EDIT(HttpStatus.CONFLICT, "FR413", "수정할 수 없는 가격 견적 요청입니다"),
    PRICE_QUOTE_CANNOT_ANALYZE(HttpStatus.CONFLICT, "FR414", "분석할 수 없는 가격 견적 요청입니다"),
    
    // BRANDING PROJECT
    BRANDING_PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "FR421", "브랜딩 프로젝트를 찾을 수 없습니다"),
    BRANDING_PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FR422", "브랜딩 프로젝트에 대한 접근 권한이 없습니다"),
    BRANDING_PROJECT_CANNOT_EDIT(HttpStatus.CONFLICT, "FR423", "수정할 수 없는 브랜딩 프로젝트입니다"),
    
    // MEMBERSHIP PLAN
    MEMBERSHIP_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "FR431", "멤버십 플랜을 찾을 수 없습니다"),
    MEMBERSHIP_PLAN_ALREADY_EXISTS(HttpStatus.CONFLICT, "FR432", "이미 존재하는 멤버십 플랜입니다"),
    
    // SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FR500", "서버 내부 오류가 발생했습니다"),
    EXTERNAL_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "FR501", "외부 API 호출에 실패했습니다");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
} 