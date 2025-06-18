package org.fr.farmranding.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "farmranding.garak")
public class GarakApiProperties {
    
    private String baseUrl = "https://temp.garak.co.kr/publicdata/dataXmlOpen.do";
    private String id;
    private String password;
    private String dataId = "gk_pumcd";
    private boolean portalTemplet = false;
    
    // API URL 생성 메서드
    public String buildApiUrl(int pageSize, int pageIndex) {
        return String.format("%s?id=%s&passwd=%s&dataid=%s&pagesize=%d&pageidx=%d&portal.templet=%s",
                baseUrl, id, password, dataId, pageSize, pageIndex, portalTemplet);
    }
} 