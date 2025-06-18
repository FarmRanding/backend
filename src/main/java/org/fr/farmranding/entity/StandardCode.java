package org.fr.farmranding.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fr.farmranding.common.entity.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "standard_codes", indexes = {
    @Index(name = "idx_standard_code_lclass", columnList = "lclassCode"),
    @Index(name = "idx_standard_code_mclass", columnList = "mclassCode"),
    @Index(name = "idx_standard_code_sclass", columnList = "sclassCode"),
    @Index(name = "idx_standard_code_mclass_name", columnList = "mclassName"),
    @Index(name = "idx_standard_code_sclass_name", columnList = "sclassName")
})
public class StandardCode extends BaseEntity {
    
    @Column(name = "lclass_code", nullable = false, length = 10)
    private String lclassCode;
    
    @Column(name = "lclass_name", nullable = false, length = 100)
    private String lclassName;
    
    @Column(name = "mclass_code", nullable = false, length = 10)
    private String mclassCode;
    
    @Column(name = "mclass_name", nullable = false, length = 100)
    private String mclassName;
    
    @Column(name = "sclass_code", nullable = false, length = 10, unique = true)
    private String sclassCode;
    
    @Column(name = "sclass_name", nullable = false, length = 100)
    private String sclassName;
    
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
} 