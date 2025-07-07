package org.fr.farmranding.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fr.farmranding.api.ProductCodeApi;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.common.validator.ProductCodeValidator;
import org.fr.farmranding.dto.request.ProductCodeSyncRequest;
import org.fr.farmranding.dto.response.ProductCodeResponse;
import org.fr.farmranding.dto.response.ProductCodeSyncResponse;
import org.fr.farmranding.service.ProductCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product-codes")
public class ProductCodeController implements ProductCodeApi {
    
    private final ProductCodeService productCodeService;
    private final ProductCodeValidator productCodeValidator;
    
    @PostMapping("/sync")
    @Override
    public ResponseEntity<FarmrandingResponseBody<ProductCodeSyncResponse>> syncProductCodes(
            @Valid @RequestBody ProductCodeSyncRequest request) {
        
        productCodeValidator.validateSyncRequest(request);
        ProductCodeSyncResponse response = productCodeService.syncProductCodes(request);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @GetMapping
    @Override
    public ResponseEntity<FarmrandingResponseBody<List<ProductCodeResponse>>> getAllActiveProductCodes() {
        
        List<ProductCodeResponse> response = productCodeService.getAllActiveProductCodes();
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @GetMapping("/search")
    @Override
    public ResponseEntity<FarmrandingResponseBody<List<ProductCodeResponse>>> searchProductCodes(
            @RequestParam(value = "keyword", required = false) String keyword) {
        
        productCodeValidator.validateSearchKeyword(keyword);
        List<ProductCodeResponse> response = productCodeService.searchProductCodes(keyword);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @GetMapping("/by-garak-code")
    @Override
    public ResponseEntity<FarmrandingResponseBody<ProductCodeResponse>> getProductCodeByGarakCode(
            @RequestParam("garakCode") String garakCode) {
        
        productCodeValidator.validateGarakCode(garakCode);
        ProductCodeResponse response = productCodeService.getProductCodeByGarakCode(garakCode);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @GetMapping("/count")
    @Override
    public ResponseEntity<FarmrandingResponseBody<Long>> getActiveProductCodeCount() {
        
        Long count = productCodeService.getActiveProductCodeCount();
        return ResponseEntity.ok(FarmrandingResponseBody.success(count));
    }
} 