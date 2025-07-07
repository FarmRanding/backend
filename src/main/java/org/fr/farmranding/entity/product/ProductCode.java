package org.fr.farmranding.entity.product;

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
@Table(name = "product_codes")
public class ProductCode extends BaseEntity {
    
    @Column(name = "garak_code", nullable = false, unique = true, length = 20)
    private String garakCode;
    
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    // 비즈니스 메서드
    public void activate() {
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void updateProductName(String newProductName) {
        this.productName = newProductName;
    }
} 