package org.fr.farmranding.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.response.StandardCodeResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "표준코드 API", description = "농림축산식품부 표준코드 관리 API")
public interface StandardCodeApi {

    @Operation(summary = "토마토 관련 표준코드 조회", description = "채소류 중 토마토 관련 표준코드 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토마토 표준코드 조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<FarmrandingResponseBody<List<StandardCodeResponse>>> getTomatoStandardCodes();
} 