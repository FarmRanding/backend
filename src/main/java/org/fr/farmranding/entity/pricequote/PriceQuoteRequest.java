package org.fr.farmranding.entity.pricequote;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
    
    // 작물 정보
    @Column(name = "crop_name", nullable = false)
    private String cropName;
    
    @Column(name = "variety")
    private String variety;
    
    @Column(name = "grade", nullable = false)
    private String grade;
    
    @Column(name = "harvest_date")
    private LocalDate harvestDate;
    
    // 가격 정보 (프론트엔드 요구사항에 맞춤)
    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice;
    
    @Column(name = "max_price", precision = 10, scale = 2)
    private BigDecimal maxPrice;
    
    @Column(name = "avg_price", precision = 10, scale = 2)
    private BigDecimal avgPrice;
    
    @Column(name = "fair_price", precision = 10, scale = 2)
    private BigDecimal fairPrice;
    
    // AI 분석 결과
    @Column(name = "analysis_result", columnDefinition = "TEXT")
    private String analysisResult;
    
    // 추가 정보
    @Column(name = "unit", nullable = false)
    private String unit;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    // 비즈니스 메서드
    public void updateBasicInfo(String cropName, String variety, String grade, LocalDate harvestDate) {
        this.cropName = cropName;
        this.variety = variety;
        this.grade = grade;
        this.harvestDate = harvestDate;
    }
    
    public void updatePriceInfo(BigDecimal minPrice, BigDecimal maxPrice, BigDecimal avgPrice, BigDecimal fairPrice, String analysisResult) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.avgPrice = avgPrice;
        this.fairPrice = fairPrice;
        this.analysisResult = analysisResult;
    }
    
    public boolean hasAnalysisResult() {
        return fairPrice != null && analysisResult != null && !analysisResult.trim().isEmpty();
    }
} 