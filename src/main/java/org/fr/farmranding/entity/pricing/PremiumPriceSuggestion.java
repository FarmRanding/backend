package org.fr.farmranding.entity.pricing;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.fr.farmranding.common.entity.BaseEntity;
import org.fr.farmranding.entity.user.User;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 프리미엄 가격 제안 기록 엔티티
 */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "premium_price_suggestions")
public class PremiumPriceSuggestion extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "item_category_code", nullable = false, length = 10)
    private String itemCategoryCode;
    
    @Column(name = "item_code", nullable = false, length = 10)
    private String itemCode;
    
    @Column(name = "kind_code", nullable = false, length = 10)
    private String kindCode;
    
    @Column(name = "product_rank_code", nullable = false, length = 10)
    private String productRankCode;
    
    @Column(name = "location", length = 50)
    private String location;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "suggested_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal suggestedPrice;
    
    @Column(name = "calculation_reason", columnDefinition = "TEXT")
    private String calculationReason;
    
    @Column(name = "retail_5day_avg", precision = 10, scale = 2)
    private BigDecimal retail5DayAvg;
    
    @Column(name = "wholesale_5day_avg", precision = 10, scale = 2)
    private BigDecimal wholesale5DayAvg;
    
    @Column(name = "alpha_ratio", precision = 10, scale = 4)
    private BigDecimal alphaRatio;
    
    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;
} 