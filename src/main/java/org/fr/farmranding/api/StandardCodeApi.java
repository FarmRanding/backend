package org.fr.farmranding.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.response.CropResponse;
import org.fr.farmranding.dto.response.VarietyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "표준코드 API", description = "농림축산식품부 표준코드 기반 작물/품종 자동완성 API")
public interface StandardCodeApi {
    
    @Operation(summary = "작물 검색", description = "작물명을 기준으로 자동완성 검색을 제공합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<FarmrandingResponseBody<List<CropResponse>>> searchCrops(
            @Parameter(description = "검색 키워드", example = "배추") 
            @RequestParam(value = "query", defaultValue = "") String query,
            
            @Parameter(description = "결과 제한 수", example = "10") 
            @RequestParam(value = "limit", defaultValue = "10") int limit
    );
    
    @Operation(summary = "인기 작물 조회", description = "품종 수가 많은 인기 작물을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<FarmrandingResponseBody<List<CropResponse>>> getPopularCrops(
            @Parameter(description = "결과 제한 수", example = "20") 
            @RequestParam(value = "limit", defaultValue = "20") int limit
    );
    
    @Operation(summary = "품종 검색", description = "특정 작물의 품종을 검색합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<FarmrandingResponseBody<List<VarietyResponse>>> searchVarieties(
            @Parameter(description = "작물 코드", example = "0303", required = true) 
            @RequestParam("cropCode") String cropCode,
            
            @Parameter(description = "검색 키워드", example = "봄") 
            @RequestParam(value = "query", defaultValue = "") String query,
            
            @Parameter(description = "결과 제한 수", example = "15") 
            @RequestParam(value = "limit", defaultValue = "15") int limit
    );
    
    @Operation(summary = "표준코드 데이터 동기화", description = "공공API에서 최신 표준코드 데이터를 가져와 동기화합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "동기화 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<FarmrandingResponseBody<String>> syncData();
} 