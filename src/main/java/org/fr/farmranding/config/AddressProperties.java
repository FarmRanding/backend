package org.fr.farmranding.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "address")
public class AddressProperties {

    /**
     * resources/ 폴더에 위치한 CSV 파일명 (빌드 시 classpath에 포함됨)
     */
    private String csvFilename;

    public String getCsvFilename() {
        return csvFilename;
    }

    public void setCsvFilename(String csvFilename) {
        this.csvFilename = csvFilename;
    }
}
