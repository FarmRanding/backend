package org.fr.farmranding.entity.pricequote;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.fr.farmranding.common.entity.BaseEntity;
import org.fr.farmranding.entity.user.User;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

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
    
    // 작물 기본 정보
    @Column(name = "crop_name", nullable = false)
    private String cropName;
    
    @Column(name = "variety")
    private String variety;
    
    @Column(name = "cultivation_method")
    private String cultivationMethod;
    
    @Column(name = "production_area")
    private String productionArea;
    
    @Column(name = "harvest_season")
    private String harvestSeason;
    
    // 품질 및 인증 정보
    @Column(name = "quality_grade")
    private String qualityGrade;
    
    @Column(name = "organic_certification")
    private Boolean organicCertification;
    
    @Column(name = "gap_certification")
    private Boolean gapCertification;
    
    @Column(name = "other_certifications")
    private String otherCertifications;
    
    // 생산 및 판매 정보
    @Column(name = "production_volume", precision = 10, scale = 2)
    private BigDecimal productionVolume;
    
    @Column(name = "production_unit")
    private String productionUnit;
    
    @Column(name = "packaging_type")
    private String packagingType;
    
    @Column(name = "packaging_size")
    private String packagingSize;
    
    // 시장 정보
    @Column(name = "target_market")
    private String targetMarket;
    
    @Column(name = "distribution_channel")
    private String distributionChannel;
    
    @Column(name = "current_selling_price", precision = 10, scale = 2)
    private BigDecimal currentSellingPrice;
    
    @Column(name = "desired_price_range")
    private String desiredPriceRange;
    
    // AI 분석 결과 (JSON으로 저장)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "market_analysis", columnDefinition = "JSON")
    private String marketAnalysis;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "price_suggestion", columnDefinition = "JSON")
    private String priceSuggestion;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "competitive_analysis", columnDefinition = "JSON")
    private String competitiveAnalysis;
    
    // 요청 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PriceQuoteStatus status;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    // 비즈니스 메서드
    public void updateBasicInfo(String cropName, String variety, String cultivationMethod, 
                               String productionArea, String harvestSeason) {
        this.cropName = cropName;
        this.variety = variety;
        this.cultivationMethod = cultivationMethod;
        this.productionArea = productionArea;
        this.harvestSeason = harvestSeason;
    }
    
    public void updateQualityInfo(String qualityGrade, Boolean organicCertification, 
                                 Boolean gapCertification, String otherCertifications) {
        this.qualityGrade = qualityGrade;
        this.organicCertification = organicCertification;
        this.gapCertification = gapCertification;
        this.otherCertifications = otherCertifications;
    }
    
    public void updateProductionInfo(BigDecimal productionVolume, String productionUnit, 
                                   String packagingType, String packagingSize) {
        this.productionVolume = productionVolume;
        this.productionUnit = productionUnit;
        this.packagingType = packagingType;
        this.packagingSize = packagingSize;
    }
    
    public void updateMarketInfo(String targetMarket, String distributionChannel, 
                               BigDecimal currentSellingPrice, String desiredPriceRange) {
        this.targetMarket = targetMarket;
        this.distributionChannel = distributionChannel;
        this.currentSellingPrice = currentSellingPrice;
        this.desiredPriceRange = desiredPriceRange;
    }
    
    public void completeAnalysis(String marketAnalysis, String priceSuggestion, String competitiveAnalysis) {
        this.marketAnalysis = marketAnalysis;
        this.priceSuggestion = priceSuggestion;
        this.competitiveAnalysis = competitiveAnalysis;
        this.status = PriceQuoteStatus.COMPLETED;
    }
    
    public void updateStatus(PriceQuoteStatus status) {
        this.status = status;
    }
    
    public void updateNotes(String notes) {
        this.notes = notes;
    }
    
    public boolean isCompleted() {
        return this.status == PriceQuoteStatus.COMPLETED;
    }
    
    public boolean canEdit() {
        return this.status == PriceQuoteStatus.DRAFT || this.status == PriceQuoteStatus.IN_PROGRESS;
    }
} 