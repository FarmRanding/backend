package org.fr.farmranding.service;

import java.util.List;
 
public interface ImageGenerationService {
    String generateBrandLogo(String brandName, List<String> keywords, String prompt);
} 