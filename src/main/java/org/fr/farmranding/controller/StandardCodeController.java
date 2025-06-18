package org.fr.farmranding.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.api.StandardCodeApi;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.response.CropResponse;
import org.fr.farmranding.dto.response.VarietyResponse;
import org.fr.farmranding.service.StandardCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/standard-codes")
public class StandardCodeController implements StandardCodeApi {
    
    private final StandardCodeService standardCodeService;
    
    @GetMapping("/crops/search")
    @Override
    public ResponseEntity<FarmrandingResponseBody<List<CropResponse>>> searchCrops(
            @RequestParam(value = "query", defaultValue = "") String query,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        try {
            List<CropResponse> crops = standardCodeService.searchCrops(query, limit);
            return ResponseEntity.ok(FarmrandingResponseBody.success(crops));
        } catch (Exception e) {
            log.error("작물 검색 실패", e);
            return ResponseEntity.internalServerError()
                .body(FarmrandingResponseBody.error(
                    FarmrandingResponseCode.INTERNAL_SERVER_ERROR.getCode(), 
                    "작물 검색에 실패했습니다."));
        }
    }
    
    @GetMapping("/crops/popular")
    @Override
    public ResponseEntity<FarmrandingResponseBody<List<CropResponse>>> getPopularCrops(
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        
        try {
            List<CropResponse> crops = standardCodeService.getPopularCrops(limit);
            return ResponseEntity.ok(FarmrandingResponseBody.success(crops));
        } catch (Exception e) {
            log.error("인기 작물 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(FarmrandingResponseBody.error(
                    FarmrandingResponseCode.INTERNAL_SERVER_ERROR.getCode(), 
                    "인기 작물 조회에 실패했습니다."));
        }
    }
    
    @GetMapping("/varieties/search")
    @Override
    public ResponseEntity<FarmrandingResponseBody<List<VarietyResponse>>> searchVarieties(
            @RequestParam("cropCode") String cropCode,
            @RequestParam(value = "query", defaultValue = "") String query,
            @RequestParam(value = "limit", defaultValue = "15") int limit) {
        
        try {
            List<VarietyResponse> varieties = standardCodeService.searchVarieties(cropCode, query, limit);
            return ResponseEntity.ok(FarmrandingResponseBody.success(varieties));
        } catch (Exception e) {
            log.error("품종 검색 실패", e);
            return ResponseEntity.internalServerError()
                .body(FarmrandingResponseBody.error(
                    FarmrandingResponseCode.INTERNAL_SERVER_ERROR.getCode(), 
                    "품종 검색에 실패했습니다."));
        }
    }
    
    @PostMapping("/sync")
    @Override
    public ResponseEntity<FarmrandingResponseBody<String>> syncData() {
        try {
            standardCodeService.syncStandardCodeData();
            return ResponseEntity.ok(FarmrandingResponseBody.success("표준코드 데이터 동기화가 시작되었습니다."));
        } catch (Exception e) {
            log.error("표준코드 데이터 동기화 실패", e);
            return ResponseEntity.internalServerError()
                .body(FarmrandingResponseBody.error(
                    FarmrandingResponseCode.INTERNAL_SERVER_ERROR.getCode(), 
                    "데이터 동기화에 실패했습니다."));
        }
    }
} 