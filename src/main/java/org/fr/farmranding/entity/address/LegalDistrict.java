package org.fr.farmranding.entity.address;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.fr.farmranding.common.entity.BaseEntity;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "legal_districts", indexes = {
    // 커버링 인덱스 (정확 일치 및 접두사 매칭용)
    @Index(name = "idx_cover_sido_address", columnList = "sido, fullAddress, districtCode"),
    @Index(name = "idx_cover_sigungu_address", columnList = "sigungu, fullAddress, districtCode"), 
    @Index(name = "idx_cover_dong_address", columnList = "dong, fullAddress, districtCode"),
    @Index(name = "idx_cover_ri_address", columnList = "ri, fullAddress, districtCode"),
    
    // 검색 최적화용 복합 인덱스
    @Index(name = "idx_search_composite", columnList = "sido, sigungu, dong, ri"),
    @Index(name = "idx_district_code", columnList = "districtCode")
})
public class LegalDistrict extends BaseEntity {
    
    @Column(name = "district_code", nullable = false, length = 10, unique = true)
    private String districtCode;
    
    @Column(name = "sido", nullable = false, length = 20)
    private String sido;
    
    @Column(name = "sigungu", length = 20)
    private String sigungu;
    
    @Column(name = "dong", length = 20)
    private String dong;
    
    @Column(name = "ri", length = 20)
    private String ri;
    
    @Column(name = "full_address", nullable = false, length = 100)
    private String fullAddress;
    
    /**
     * 전체 주소 생성 및 설정
     */
    @PrePersist
    @PreUpdate
    private void generateFullAddress() {
        StringBuilder fullAddressBuilder = new StringBuilder();
        
        if (sido != null && !sido.trim().isEmpty()) {
            fullAddressBuilder.append(sido);
        }
        if (sigungu != null && !sigungu.trim().isEmpty()) {
            fullAddressBuilder.append(" ").append(sigungu);
        }
        if (dong != null && !dong.trim().isEmpty()) {
            fullAddressBuilder.append(" ").append(dong);
        }
        if (ri != null && !ri.trim().isEmpty()) {
            fullAddressBuilder.append(" ").append(ri);
        }
        
        this.fullAddress = fullAddressBuilder.toString().trim();
    }
} 