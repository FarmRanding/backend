package org.fr.farmranding.entity.pricing;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.fr.farmranding.common.entity.BaseEntity;

/**
 * KAMIS 품목 코드 정보 엔티티
 * kamisCode.csv 파일의 데이터를 저장
 */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "kamis_product_codes")
public class KamisProductCode extends BaseEntity {
    
    @Column(name = "group_code", nullable = false, length = 10)
    private String groupCode;
    
    @Column(name = "group_name", nullable = false, length = 50)
    private String groupName;
    
    @Column(name = "item_code", nullable = false, length = 10)
    private String itemCode;
    
    @Column(name = "item_name", nullable = false, length = 50)
    private String itemName;
    
    @Column(name = "kind_code", nullable = false, length = 10)
    private String kindCode;
    
    @Column(name = "kind_name", nullable = false, length = 50)
    private String kindName;
}
 