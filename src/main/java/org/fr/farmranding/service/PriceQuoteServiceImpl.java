package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.dto.pricequote.PriceQuoteCreateRequest;
import org.fr.farmranding.dto.pricequote.PriceQuoteResponse;
import org.fr.farmranding.dto.pricequote.PriceQuoteUpdateRequest;
import org.fr.farmranding.entity.pricequote.PriceQuoteRequest;
import org.fr.farmranding.entity.pricequote.PriceQuoteStatus;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.repository.PriceQuoteRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PriceQuoteServiceImpl implements PriceQuoteService {
    
    private final PriceQuoteRequestRepository priceQuoteRequestRepository;
    private final UserService userService;
    
    @Override
    public PriceQuoteResponse createPriceQuoteRequest(PriceQuoteCreateRequest request, User currentUser) {
        // 가격 제안 사용량 체크
        userService.incrementPricingSuggestionUsage(currentUser.getId());
        
        PriceQuoteRequest priceQuoteRequest = PriceQuoteRequest.builder()
                .user(currentUser)
                .cropName(request.cropName())
                .variety(request.variety())
                .cultivationMethod(request.cultivationMethod())
                .productionArea(request.productionArea())
                .harvestSeason(request.harvestSeason())
                .qualityGrade(request.qualityGrade())
                .organicCertification(request.organicCertification())
                .gapCertification(request.gapCertification())
                .otherCertifications(request.otherCertifications())
                .productionVolume(request.productionVolume())
                .productionUnit(request.productionUnit())
                .packagingType(request.packagingType())
                .packagingSize(request.packagingSize())
                .targetMarket(request.targetMarket())
                .distributionChannel(request.distributionChannel())
                .currentSellingPrice(request.currentSellingPrice())
                .desiredPriceRange(request.desiredPriceRange())
                .notes(request.notes())
                .status(PriceQuoteStatus.DRAFT)
                .build();
        
        PriceQuoteRequest savedRequest = priceQuoteRequestRepository.save(priceQuoteRequest);
        log.info("가격 제안 요청 생성 완료: requestId={}, userId={}", savedRequest.getId(), currentUser.getId());
        
        return PriceQuoteResponse.from(savedRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PriceQuoteResponse getPriceQuoteRequest(Long requestId, User currentUser) {
        PriceQuoteRequest request = findRequestByIdAndUser(requestId, currentUser.getId());
        return PriceQuoteResponse.from(request);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PriceQuoteResponse> getUserPriceQuoteRequests(User currentUser) {
        List<PriceQuoteRequest> requests = priceQuoteRequestRepository.findByUserId(currentUser.getId());
        return requests.stream()
                .map(PriceQuoteResponse::from)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PriceQuoteResponse> getPriceQuoteRequestsByStatus(PriceQuoteStatus status, User currentUser) {
        List<PriceQuoteRequest> requests = priceQuoteRequestRepository.findByUserIdAndStatus(currentUser.getId(), status);
        return requests.stream()
                .map(PriceQuoteResponse::from)
                .toList();
    }
    
    @Override
    public PriceQuoteResponse updatePriceQuoteRequest(Long requestId, PriceQuoteUpdateRequest request, User currentUser) {
        PriceQuoteRequest priceQuoteRequest = findRequestByIdAndUser(requestId, currentUser.getId());
        
        if (!priceQuoteRequest.canEdit()) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
        
        // 기본 정보 업데이트
        if (request.cropName() != null || request.variety() != null || request.cultivationMethod() != null ||
            request.productionArea() != null || request.harvestSeason() != null) {
            priceQuoteRequest.updateBasicInfo(
                    request.cropName() != null ? request.cropName() : priceQuoteRequest.getCropName(),
                    request.variety(),
                    request.cultivationMethod(),
                    request.productionArea(),
                    request.harvestSeason()
            );
        }
        
        // 품질 정보 업데이트
        if (request.qualityGrade() != null || request.organicCertification() != null ||
            request.gapCertification() != null || request.otherCertifications() != null) {
            priceQuoteRequest.updateQualityInfo(
                    request.qualityGrade(),
                    request.organicCertification(),
                    request.gapCertification(),
                    request.otherCertifications()
            );
        }
        
        // 생산 정보 업데이트
        if (request.productionVolume() != null || request.productionUnit() != null ||
            request.packagingType() != null || request.packagingSize() != null) {
            priceQuoteRequest.updateProductionInfo(
                    request.productionVolume(),
                    request.productionUnit(),
                    request.packagingType(),
                    request.packagingSize()
            );
        }
        
        // 시장 정보 업데이트
        if (request.targetMarket() != null || request.distributionChannel() != null ||
            request.currentSellingPrice() != null || request.desiredPriceRange() != null) {
            priceQuoteRequest.updateMarketInfo(
                    request.targetMarket(),
                    request.distributionChannel(),
                    request.currentSellingPrice(),
                    request.desiredPriceRange()
            );
        }
        
        // 비고 업데이트
        if (request.notes() != null) {
            priceQuoteRequest.updateNotes(request.notes());
        }
        
        PriceQuoteRequest savedRequest = priceQuoteRequestRepository.save(priceQuoteRequest);
        log.info("가격 제안 요청 수정 완료: requestId={}", requestId);
        
        return PriceQuoteResponse.from(savedRequest);
    }
    
    @Override
    public void deletePriceQuoteRequest(Long requestId, User currentUser) {
        PriceQuoteRequest request = findRequestByIdAndUser(requestId, currentUser.getId());
        
        priceQuoteRequestRepository.delete(request);
        log.info("가격 제안 요청 삭제 완료: requestId={}", requestId);
    }
    
    @Override
    public PriceQuoteResponse analyzePriceQuote(Long requestId, User currentUser) {
        PriceQuoteRequest request = findRequestByIdAndUser(requestId, currentUser.getId());
        
        if (!request.canAnalyze()) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
        
        // TODO: 실제 AI 가격 분석 로직 구현 (외부 API 연동)
        // 현재는 더미 데이터로 분석 완료 처리
        String marketAnalysis = "{\"analysis\": \"AI가 분석한 시장 동향\", \"trend\": \"상승\"}";
        String priceSuggestion = "{\"suggested_price\": 18000, \"min_price\": 16000, \"max_price\": 20000}";
        String competitiveAnalysis = "{\"competitors\": [\"경쟁사A\", \"경쟁사B\"], \"position\": \"중간\"}";
        
        request.completeAnalysis(marketAnalysis, priceSuggestion, competitiveAnalysis);
        PriceQuoteRequest savedRequest = priceQuoteRequestRepository.save(request);
        
        log.info("가격 분석 완료: requestId={}", requestId);
        return PriceQuoteResponse.from(savedRequest);
    }
    
    @Override
    public PriceQuoteResponse updateRequestStatus(Long requestId, PriceQuoteStatus status, User currentUser) {
        PriceQuoteRequest request = findRequestByIdAndUser(requestId, currentUser.getId());
        
        request.updateStatus(status);
        PriceQuoteRequest savedRequest = priceQuoteRequestRepository.save(request);
        
        log.info("요청 상태 변경 완료: requestId={}, status={}", requestId, status);
        return PriceQuoteResponse.from(savedRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PriceQuoteResponse> getPriceQuoteRequestsByCrop(String cropName, User currentUser) {
        List<PriceQuoteRequest> requests = priceQuoteRequestRepository.findByUserIdAndCropNameContaining(currentUser.getId(), cropName);
        return requests.stream()
                .map(PriceQuoteResponse::from)
                .toList();
    }
    
    private PriceQuoteRequest findRequestByIdAndUser(Long requestId, Long userId) {
        return priceQuoteRequestRepository.findByIdAndUserId(requestId, userId)
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.USER_NOT_FOUND));
    }
} 