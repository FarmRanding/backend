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
    
    // ========== 기본 응답 ==========
    SUCCESS(100, "요청이 성공적으로 처리되었습니다.", HttpStatus.OK),
    BAD_REQUEST(400, "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND(404, "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // ========== 비즈니스 로직 관련 상세 오류 ==========
    VALIDATION_ERROR(1000, "유효성 검사에 실패했습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_RESOURCE(1001, "중복된 데이터가 존재합니다.", HttpStatus.CONFLICT),
    RESOURCE_CONFLICT(1002, "리소스 충돌이 발생했습니다.", HttpStatus.CONFLICT),
    DATABASE_ERROR(1003, "데이터베이스 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    REGISTRATION_FAILED(1004, "데이터 등록에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_FAILED(1005, "파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PARAMETER_ERROR(1006, "파라미터가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    BAD_JSON_FORMAT(1007, "요청 형식이 올바르지 않습니다. 입력 값을 확인해주세요.", HttpStatus.BAD_REQUEST),
    
    // ========== 인증/권한 관련 ==========
    JWT_EXPIRED(1100, "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    JWT_MALFORMED(1101, "유효하지 않은 토큰 형식입니다.", HttpStatus.UNAUTHORIZED),
    JWT_BADSIGN(1102, "토큰 서명 검증 실패", HttpStatus.UNAUTHORIZED),
    JWT_UNSUPPORTED(1103, "지원되지 않는 토큰입니다.", HttpStatus.UNAUTHORIZED),
    JWT_DECODING(1104, "토큰 디코딩 오류입니다.", HttpStatus.UNAUTHORIZED),
    JWT_ILLEGAL(1105, "토큰이 비어있거나 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    JWT_UNAUTHORIZED(1106, "토큰 검증 중 오류가 발생했습니다.", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_SOCIAL_PROVIDER(1107, "지원하지 않는 소셜 로그인 제공자입니다.", HttpStatus.BAD_REQUEST),
    
    // ========== 사용자 관련 ==========
    USER_NOT_FOUND(1200, "해당 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL(1201, "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    
    // ========== 작물 관련 ==========
    CROP_NOT_FOUND(1300, "작물 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_CROP_SEASON(1301, "작물의 재배 시기가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    
    // ========== 브랜딩 관련 ==========
    BRANDING_NOT_FOUND(1400, "브랜딩 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    AI_GENERATION_FAILED(1401, "AI 브랜딩 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // ========== 멤버십 관련 ==========
    MEMBERSHIP_REQUIRED(1500, "프리미엄 멤버십이 필요한 기능입니다.", HttpStatus.FORBIDDEN),
    USAGE_LIMIT_EXCEEDED(1501, "사용 한도를 초과했습니다.", HttpStatus.FORBIDDEN),
    INSUFFICIENT_MEMBERSHIP(1502, "현재 멤버십으로는 이용할 수 없는 기능입니다.", HttpStatus.FORBIDDEN),
    
    // ========== 외부 API 관련 ==========
    OPENAI_API_ERROR(1600, "OpenAI API 호출에 실패했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    MARKET_DATA_UNAVAILABLE(1601, "시장 데이터를 가져올 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    EXTERNAL_API_ERROR(1602, "외부 API 호출에 실패했습니다.", HttpStatus.SERVICE_UNAVAILABLE);
    
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
} 