package org.fr.farmranding.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.request.ProductCodeSyncRequest;
import org.fr.farmranding.dto.response.ProductCodeResponse;
import org.fr.farmranding.dto.response.ProductCodeSyncResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "품목 코드 API", description = "가락시장 품목 코드 관리 API")
public interface ProductCodeApi {
    
    @Operation(summary = "품목 코드 동기화", description = "가락시장 API로부터 품목 코드를 동기화합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "동기화 성공",
                content = @Content(schema = @Schema(implementation = ProductCodeSyncResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "503", description = "가락시장 API 호출 실패")
    })
    ResponseEntity<FarmrandingResponseBody<ProductCodeSyncResponse>> syncProductCodes(
            @Valid @RequestBody ProductCodeSyncRequest request);
    
    @Operation(summary = "활성 품목 코드 전체 조회", description = "모든 활성 품목 코드를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<FarmrandingResponseBody<List<ProductCodeResponse>>> getAllActiveProductCodes();
    
    @Operation(summary = "품목 코드 검색", description = "키워드로 품목 코드를 검색합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<FarmrandingResponseBody<List<ProductCodeResponse>>> searchProductCodes(
            @Parameter(description = "검색 키워드", example = "감자")
            @RequestParam(value = "keyword", required = false) String keyword);
    
    @Operation(summary = "가락시장 코드로 품목 조회", description = "가락시장 코드로 특정 품목을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "품목 코드를 찾을 수 없음")
    })
    ResponseEntity<FarmrandingResponseBody<ProductCodeResponse>> getProductCodeByGarakCode(
            @Parameter(description = "가락시장 품목 코드", example = "15200")
            @RequestParam("garakCode") String garakCode);
    
    @Operation(summary = "활성 품목 코드 개수 조회", description = "활성 상태인 품목 코드의 총 개수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<FarmrandingResponseBody<Long>> getActiveProductCodeCount();
} 