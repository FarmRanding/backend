package org.fr.farmranding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.address.LegalDistrictResponse;
import org.fr.farmranding.service.AddressService;
import org.fr.farmranding.service.AddressServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/address")
@Tag(name = "주소 API", description = "법정동 주소 검색 관련 API")
public class AddressController {
    
    private final AddressService addressService;
    private final AddressServiceImpl addressServiceImpl;
    
    @GetMapping("/search")
    @Operation(summary = "법정동 검색", description = "키워드로 법정동을 검색합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<FarmrandingResponseBody<List<LegalDistrictResponse>>> searchLegalDistricts(
            @Parameter(description = "검색 키워드", example = "부산")
            @RequestParam(name = "keyword") String keyword,
            
            @Parameter(description = "최대 결과 수", example = "20")
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        
        log.info("법정동 검색 요청: keyword={}, limit={}", keyword, limit);
        
        List<LegalDistrictResponse> results = addressService.searchLegalDistricts(keyword, limit);
        
        log.info("법정동 검색 결과: {}개", results.size());
        
        return ResponseEntity.ok(FarmrandingResponseBody.success(results));
    }
    
    @GetMapping("/sido/{sido}")
    @Operation(summary = "시도별 법정동 조회", description = "특정 시도의 법정동 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<FarmrandingResponseBody<List<LegalDistrictResponse>>> getDistrictsBySido(
            @Parameter(description = "시도명", example = "경기도")
            @PathVariable String sido,
            
            @Parameter(description = "최대 결과 수", example = "50")
            @RequestParam(name = "limit", defaultValue = "50") int limit) {
        
        log.info("시도별 법정동 조회 요청: sido={}, limit={}", sido, limit);
        
        List<LegalDistrictResponse> results = addressServiceImpl.getDistrictsBySido(sido, limit);
        
        log.info("시도별 법정동 조회 결과: {}개", results.size());
        
        return ResponseEntity.ok(FarmrandingResponseBody.success(results));
    }
    
    @GetMapping("/code/{districtCode}")
    @Operation(summary = "법정동 코드로 상세 조회", description = "법정동 코드로 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "해당 코드의 법정동을 찾을 수 없음")
    })
    public ResponseEntity<FarmrandingResponseBody<LegalDistrictResponse>> getDistrictByCode(
            @Parameter(description = "법정동 코드", example = "1111010100")
            @PathVariable String districtCode) {
        
        log.info("법정동 코드 조회 요청: districtCode={}", districtCode);
        
        LegalDistrictResponse result = addressServiceImpl.getDistrictByCode(districtCode);
        
        if (result == null) {
            log.warn("법정동 코드를 찾을 수 없음: {}", districtCode);
            return ResponseEntity.notFound().build();
        }
        
        log.info("법정동 코드 조회 성공: {}", result.fullAddress());
        
        return ResponseEntity.ok(FarmrandingResponseBody.success(result));
    }
} 