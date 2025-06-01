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
    
    // 기본 작물 정보 (프론트엔드 요구사항에 맞춤)
    @Column(name = "crop_name", nullable = false)
    private String cropName;
    
    @Column(name = "variety")
    private String variety;
    
    @Column(name = "grade", nullable = false)
    private String grade;
    
    @Column(name = "harvest_date")
    private LocalDate harvestDate;
    
    // 가격 정보
    @Column(name = "estimated_price", precision = 10, scale = 2)
    private BigDecimal estimatedPrice;
    
    @Column(name = "final_price", precision = 10, scale = 2)
    private BigDecimal finalPrice;
    
    // 분석 결과
    @Column(name = "analysis_result", columnDefinition = "TEXT")
    private String analysisResult;
    
    // 상태 관리
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PriceQuoteStatus status;
    
    // 비즈니스 메서드
    public void updateBasicInfo(String cropName, String variety, String grade, 
                               LocalDate harvestDate, BigDecimal estimatedPrice) {
        this.cropName = cropName;
        this.variety = variety;
        this.grade = grade;
        this.harvestDate = harvestDate;
        this.estimatedPrice = estimatedPrice;
        this.status = PriceQuoteStatus.PENDING;
    }
    
    public void completeAnalysis(BigDecimal finalPrice, String analysisResult) {
        this.finalPrice = finalPrice;
        this.analysisResult = analysisResult;
        this.status = PriceQuoteStatus.COMPLETED;
    }
    
    public void updateStatus(PriceQuoteStatus status) {
        this.status = status;
    }
    
    public boolean isCompleted() {
        return this.status == PriceQuoteStatus.COMPLETED;
    }
    
    public boolean canEdit() {
        return this.status.canEdit();
    }
    
    public boolean hasEstimatedPrice() {
        return this.estimatedPrice != null && this.estimatedPrice.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean hasFinalPrice() {
        return this.finalPrice != null && this.finalPrice.compareTo(BigDecimal.ZERO) > 0;
    }
} 