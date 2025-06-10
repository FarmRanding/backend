package org.fr.farmranding.entity.pricequote;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.fr.farmranding.common.entity.BaseEntity;
import org.fr.farmranding.entity.user.User;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "price_quote_requests")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PriceQuoteRequest extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // 품목 정보 (ProductCode 연동)
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "garak_code", nullable = false, length = 20)
    private String garakCode;
    
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;
    
    @Column(name = "grade", nullable = false)
    private String grade;
    
    @Column(name = "harvest_date")
    private LocalDate harvestDate;
    
    @Column(name = "unit", nullable = false)
    private String unit;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    // 견적 요청 가격
    @Column(name = "estimated_price", precision = 10, scale = 2)
    private BigDecimal estimatedPrice;
    
    // 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PriceQuoteStatus status = PriceQuoteStatus.DRAFT;
    
    // AI 분석 가격 정보 (프론트엔드 요구사항에 맞춤)
    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice;
    
    @Column(name = "max_price", precision = 10, scale = 2)
    private BigDecimal maxPrice;
    
    @Column(name = "avg_price", precision = 10, scale = 2)
    private BigDecimal avgPrice;
    
    @Column(name = "fair_price", precision = 10, scale = 2)
    private BigDecimal fairPrice;
    
    // AI 분석 결과 텍스트
    @Column(name = "analysis_result", columnDefinition = "TEXT")
    private String analysisResult;
    
    // 최종 분석 가격
    @Column(name = "final_price", precision = 10, scale = 2)
    private BigDecimal finalPrice;
    
    // 비즈니스 메서드
    public void updateBasicInfo(Long productId, String garakCode, String productName, String grade, LocalDate harvestDate, BigDecimal estimatedPrice) {
        this.productId = productId;
        this.garakCode = garakCode;
        this.productName = productName;
        this.grade = grade;
        this.harvestDate = harvestDate;
        this.estimatedPrice = estimatedPrice;
    }
    
    public void updateBasicInfo(Long productId, String garakCode, String productName, String grade, LocalDate harvestDate, String unit, Integer quantity) {
        this.productId = productId;
        this.garakCode = garakCode;
        this.productName = productName;
        this.grade = grade;
        this.harvestDate = harvestDate;
        this.unit = unit;
        this.quantity = quantity;
    }
    
    public void updatePriceAnalysis(BigDecimal minPrice, BigDecimal maxPrice, BigDecimal avgPrice, BigDecimal fairPrice, String analysisResult) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.avgPrice = avgPrice;
        this.fairPrice = fairPrice;
        this.analysisResult = analysisResult;
    }
    
    public void updateStatus(PriceQuoteStatus status) {
        this.status = status;
    }
    
    public void completeAnalysis(BigDecimal finalPrice, String analysisResult) {
        this.finalPrice = finalPrice;
        this.analysisResult = analysisResult;
        this.status = PriceQuoteStatus.COMPLETED;
    }
    
    public boolean canEdit() {
        return status == PriceQuoteStatus.DRAFT;
    }
    
    public boolean hasAnalysisResult() {
        return fairPrice != null && analysisResult != null && !analysisResult.trim().isEmpty();
    }
    
    public boolean isAnalysisComplete() {
        return hasAnalysisResult() && minPrice != null && maxPrice != null && avgPrice != null;
    }
} 