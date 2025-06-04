package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.dto.branding.BrandingProjectCreateRequest;
import org.fr.farmranding.dto.branding.BrandingProjectResponse;
import org.fr.farmranding.dto.branding.BrandingProjectUpdateRequest;
import org.fr.farmranding.dto.branding.BrandNameRequest;
import org.fr.farmranding.entity.branding.BrandingProject;
import org.fr.farmranding.entity.branding.ImageGenerationStatus;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.repository.BrandingProjectRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BrandingServiceImpl implements BrandingService {
    
    private final BrandingProjectRepository brandingProjectRepository;
    private final UserService userService;
    private final ChatModel chatModel;
    private final ImageGenerationService imageGenerationService;
    
    private static final String BRAND_NAME_PROMPT_TEMPLATE =
        "사용자에게 질문하여 정보 수집 후 사용자 지정 작물에 적합한 브랜드명을 생성하세요.\n" +
        "\n" +
        "수집할 정보:\n" +
        "1. **작물명 및 품종:** {cropName}, {variety}\n" +
        "2. **사용자가 원하는 브랜드 이미지:** {brandingKeywords}\n" +
        "3. **작물이 가진 매력:** {cropAppealKeywords}\n" +
        "\n" +
        "# Steps\n" +
        "1. 사용자가 제공한 작물명 및 품종을 분석합니다.\n" +
        "2. 사용자가 원하는 브랜드 이미지를 파악합니다. 이는 브랜드가 전달하고자 하는 느낌이나 인상을 의미합니다.\n" +
        "3. 작물이 가지는 매력을 분석합니다. 작물이 가지는 독특한 장점을 중점적으로 고려합니다.\n" +
        "\n" +
        "# Output Format\n" +
        "**🚨 중요: 브랜드명만 출력하세요. 마크다운, 설명, 문장, 특수문자는 절대 포함하지 마세요.**\n" +
        "\n" +
        "출력 예시:\n" +
        "아삭체리탑\n" +
        "신선토마토\n" +
        "자연방울\n" +
        "\n" +
        "- **브랜드명:** 짧고 기억에 남는 형태로, 2개의 단어 내외로 구성합니다.\n" +
        "- 브랜드명은 사용자가 제공한 작물명, 품종, 브랜드 이미지, 작물의 매력 요소를 분석하여 생성합니다.\n" +
        "- 다른 농가와 차별화될 만한 요소를 반영하여 소비자에게 매력적으로 다가갈 수 있는 요소를 포함합니다.\n" +
        "\n" +
        "# Examples\n" +
        "- 정보 입력:\n" +
        "    - 작물명 및 품종: 토마토, 체리\n" +
        "    - 브랜드 이미지: 신선하고 건강한 느낌\n" +
        "    - 작물의 매력: 고당도의 맛과 아삭한 식감\n" +
        "- 출력:\n" +
        "    - 아삭체리탑\n" +
        "\n" +
        "# Notes\n" +
        "- 브랜드명 생성 시 농가 위치나 다른 배경 정보를 활용하여 신선하고 참신한 느낌을 주도록 고려합니다.\n" +
        "- 브랜드명이 직관적이고 긍정적인 인상을 줄 수 있도록 하여야 합니다.\n" +
        "- 각 단계에서 얻은 정보를 충실히 반영해주세요.\n" +
        "- **응답은 브랜드명만 한 줄로 출력하세요. 다른 텍스트, 마크다운, 특수문자는 포함하지 마세요.**";

    private static final String CONCEPT_AND_STORY_PROMPT_TEMPLATE =
        "작물의 홍보 문구와 판매글을 JSON 형식으로 생성해주세요.\n" +
        "\n" +
        "## 📋 작성 요구사항\n" +
        "\n" +
        "### 홍보 문구 (concept) 규칙:\n" +
        "- **길이**: 정확히 15자 이상 35자 이하 (필수)\n" +
        "- **형식**: 한 줄의 매력적인 문구\n" +
        "- **필수 조건**: 반드시 명사로 끝나야 함 (예: '맛', '토마토', '감동', '선택')\n" +
        "- **내용**: 작물의 특성을 강조하되 브랜드명은 절대 포함하지 않음\n" +
        "- **예시**: \"자연이 키운 달콤한 토마토\", \"햇살 머금은 신선한 맛\"\n" +
        "\n" +
        "### 판매글 (story) 규칙:\n" +
        "- **길이**: 최소 350자 이상 작성 (400-600자 권장)\n" +
        "- **구성**: 농장 소개 → 재배 과정 → 품질/맛 → 구매 유도\n" +
        "- **포함 요소**: 제공된 모든 정보를 의미있게 연결한 상세한 스토리\n" +
        "- **농가명 활용**: 농가명이 제공된 경우 판매글에 자연스럽게 포함시켜 브랜드 신뢰성 향상\n" +
        "- **어조**: 구체적이고 신뢰감 있는 설명\n" +
        "- **중요**: 반드시 350자 이상으로 충분히 길게 작성해주세요\n" +
        "\n" +
        "## 💡 길이 확인 중요 안내\n" +
        "- **홍보문구**: 15-35자 범위에서 정확히 작성\n" +
        "- **판매글**: 최소 350자 이상, 가능하면 400-600자로 상세하게 작성\n" +
        "- **판매글 길이 체크**: 반드시 350자 이상인지 확인 후 제출\n" +
        "- 판매글이 너무 짧으면 농장 스토리, 재배 과정, 품질 설명을 더 추가하세요\n" +
        "\n" +
        "## 🏪 농가명 활용 가이드\n" +
        "- **농가명이 제공된 경우**: 판매글에 농가명을 자연스럽게 포함하여 신뢰성과 브랜드 인지도 향상\n" +
        "- **농가명이 없는 경우**: 일반적인 농장 표현 사용 (예: '저희 농장', '우리 농장')\n" +
        "- **사용 예시**: '[농가명]에서 정성스럽게 키운...', '[농가명]의 전통 농법으로...'\n" +
        "\n" +
        "## ⚠️ 필수 출력 형식 (JSON)\n" +
        "```json\n" +
        "{\n" +
        "  \"concept\": \"15-35자 이내, 명사로 끝나는 홍보 문구\",\n" +
        "  \"story\": \"최소 350자 이상의 상세한 판매 글 (농가명이 있다면 포함)\"\n" +
        "}\n" +
        "```\n" +
        "\n" +
        "## 🔧 농가명 처리 로직\n" +
        "- 농가명이 비어있지 않은 경우: 판매글에 농가명을 활용한 스토리 작성\n" +
        "- 농가명이 비어있는 경우: 농가명 없이 일반적인 농장 표현으로 작성\n" +
        "\n" +
        "## 📝 추가 지침\n" +
        "- 모든 제공된 정보(작물명, 품종, 재배방식, 등급, 위치, 키워드)를 적절히 활용\n" +
        "- 소비자에게 신뢰감과 구매 욕구를 불러일으키는 내용 작성\n" +
        "- 농산물의 특별함과 품질을 강조하여 차별화된 가치 전달\n";

    private static final String LOGO_PROMPT_TEMPLATE =
        "Create a professional agricultural logo design based on the following specifications:\n" +
        "\n" +
        "**Brand Information:**\n" +
        "- Brand Name: '{brandName}'\n" +
        "- Crop Name: {cropName}\n" +
        "- Combined Keywords: {keywords}\n" +
        "\n" +
        "**Design Requirements:**\n" +
        "\n" +
        "1. **Central Image (Crop) Description:**\n" +
        "   - Feature {cropName} as the main visual element with specific characteristics (color, shape, texture)\n" +
        "   - Ensure the crop illustration is clean, detailed, and professionally rendered\n" +
        "   - Emphasize the natural beauty and quality of the produce\n" +
        "\n" +
        "2. **Brand Atmosphere & Color Tone:**\n" +
        "   - Apply colors and overall tone that match the keywords: {keywords}\n" +
        "   - Use vibrant, fresh colors that convey quality and freshness\n" +
        "   - Maintain a natural, agricultural aesthetic\n" +
        "\n" +
        "3. **Crop Appeal Points:**\n" +
        "   - Reflect organic, pesticide-free, or premium quality aspects in the visual design\n" +
        "   - Emphasize freshness and natural cultivation methods\n" +
        "\n" +
        "4. **Typography Instructions:**\n" +
        "   - Display the brand name '{brandName}' prominently with a font that matches the agricultural theme\n" +
        "   - Ensure the font is readable, modern, and harmonious with the overall design\n" +
        "   - Text should complement the crop illustration perfectly\n" +
        "\n" +
        "**Layout Composition:**\n" +
        "- Position the crop illustration in the center or upper-center\n" +
        "- Place the brand name '{brandName}' below or integrated with the crop image\n" +
        "- Add appropriate margins, outlines, or shadow effects if necessary\n" +
        "- Ensure text and graphics work together harmoniously\n" +
        "\n" +
        "**Technical Specifications:**\n" +
        "- Size: 1024x1024 pixels, high quality\n" +
        "- Style: Clean vector style with modern typography\n" +
        "- Background: Clean white or transparent\n" +
        "- Professional layout suitable for agricultural branding\n" +
        "\n" +
        "**Final Requirements:**\n" +
        "- The logo must clearly represent the agricultural brand identity\n" +
        "- All elements should work cohesively to create a memorable brand mark\n" +
        "- Ensure the design is scalable and works well in various sizes";
    
    @Override
    public BrandingProjectResponse createBrandingProject(BrandingProjectCreateRequest request, User currentUser) {
        // AI 브랜딩 사용량 체크
        userService.incrementAiBrandingUsage(currentUser.getId());
        
        // 기본 브랜드명 생성 (Fallback)
        String fallbackBrandName = generateFallbackBrandName(request.cropName(), request.brandingKeywords());
        
        BrandingProject project = BrandingProject.builder()
                .title(request.title())
                .user(currentUser)
                .cropName(request.cropName())
                .variety(request.variety())
                .cultivationMethod(request.cultivationMethod())
                .grade(request.grade())
                .includeFarmName(request.includeFarmName())
                .brandingKeywords(request.brandingKeywords())
                .cropAppealKeywords(request.cropAppealKeywords())
                .logoImageKeywords(request.logoImageKeywords())
                .generatedBrandName(fallbackBrandName)
                .brandImageUrl(null) // 이미지 없이 기본 프로젝트 생성
                .brandConcept(fallbackBrandName + "과 함께하는 건강한 삶")
                .brandStory("정성과 사랑으로 키운 " + fallbackBrandName + "입니다. 자연 그대로의 맛과 영양을 담아, 건강한 식탁을 만들어가는 브랜드입니다. " +
                        "우리 농장은 깨끗한 환경에서 친환경적인 재배 방식을 통해 최고 품질의 농산물을 생산합니다. " +
                        "각각의 작물은 정성스럽게 관리되어 신선함과 맛을 극대화했으며, 엄격한 품질 관리를 통해 소비자에게 안전하고 건강한 먹거리를 제공합니다. " +
                        fallbackBrandName + "의 특별함을 직접 경험해보세요. 자연이 선사하는 진정한 맛의 감동을 느낄 수 있을 것입니다. " +
                        "건강한 가족의 식탁을 위한 최고의 선택, " + fallbackBrandName + "을 만나보세요.")
                .build();
        
        BrandingProject savedProject = brandingProjectRepository.save(project);
        log.info("브랜딩 프로젝트 생성 완료: projectId={}, userId={}", savedProject.getId(), currentUser.getId());
        
        return BrandingProjectResponse.from(savedProject);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BrandingProjectResponse getBrandingProject(Long projectId, User currentUser) {
        BrandingProject project = findProjectByIdAndUser(projectId, currentUser.getId());
        return BrandingProjectResponse.from(project);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BrandingProjectResponse> getUserBrandingProjects(User currentUser) {
        List<BrandingProject> projects = brandingProjectRepository.findByUserId(currentUser.getId());
        return projects.stream()
                .map(BrandingProjectResponse::from)
                .toList();
    }

    
    @Override
    public BrandingProjectResponse updateBrandingProject(Long projectId, BrandingProjectUpdateRequest request, User currentUser) {
        BrandingProject project = findProjectByIdAndUser(projectId, currentUser.getId());

        // 기본 정보 업데이트
        if (request.title() != null || request.cropName() != null) {
            project.updateBasicInfo(
                    request.title() != null ? request.title() : project.getTitle(),
                    request.cropName() != null ? request.cropName() : project.getCropName(),
                    request.variety(),
                    request.cultivationMethod(),
                    request.grade()
            );
        }
        
        // GAP 정보 업데이트
        if (request.gapNumber() != null || request.isGapVerified() != null) {
            project.updateGapInfo(request.gapNumber(), request.isGapVerified());
        }
        
        // 키워드 정보 업데이트
        if (request.brandingKeywords() != null) {
            project.updateBrandingKeywords(request.brandingKeywords());
        }
        if (request.cropAppealKeywords() != null) {
            project.updateCropAppealKeywords(request.cropAppealKeywords());
        }
        if (request.logoImageKeywords() != null) {
            project.updateLogoImageKeywords(request.logoImageKeywords());
        }
        
        BrandingProject savedProject = brandingProjectRepository.save(project);
        log.info("브랜딩 프로젝트 수정 완료: projectId={}", projectId);
        
        return BrandingProjectResponse.from(savedProject);
    }
    
    @Override
    public void deleteBrandingProject(Long projectId, User currentUser) {
        BrandingProject project = findProjectByIdAndUser(projectId, currentUser.getId());
        
        brandingProjectRepository.delete(project);
        log.info("브랜딩 프로젝트 삭제 완료: projectId={}", projectId);
    }
    
    @Override
    public String generateBrandName(BrandNameRequest request, User currentUser, String prompt) {
        // AI 브랜딩 사용량 체크
        userService.validateAiBrandingUsage(currentUser.getId());
        
        // 새로운 브랜드명 생성 프롬프트 조합
        String brandNamePrompt = BRAND_NAME_PROMPT_TEMPLATE
                .replace("{cropName}", request.cropName())
                .replace("{variety}", request.variety() != null ? request.variety() : "일반 품종")
                .replace("{brandingKeywords}", String.join(", ", request.brandingKeywords()))
                .replace("{cropAppealKeywords}", 
                    request.cropAppealKeywords() != null && !request.cropAppealKeywords().isEmpty() 
                        ? String.join(", ", request.cropAppealKeywords())
                        : String.join(", ", request.brandingKeywords()) // fallback
                );
        
        log.info("브랜드명 생성 시작: cropName={}, variety={}, brandingKeywords={}, cropAppealKeywords={}", 
                request.cropName(), request.variety(), request.brandingKeywords(), request.cropAppealKeywords());
        
        // 재시도 로직 (최대 3회)
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                log.info("브랜드명 생성 시도 {}/3: cropName={}", attempt, request.cropName());
                
                // ChatModel을 사용한 브랜드명 생성
                ChatResponse response = chatModel.call(
                    new Prompt(brandNamePrompt, OpenAiChatOptions.builder()
                        .model("gpt-4o-mini")
                        .maxTokens(20) // 브랜드명은 매우 짧으므로 토큰 수 더 줄임
                        .temperature(0.8) // 창의성 높이기
                        .build())
                );
                
                String generatedBrandName = response.getResult().getOutput().getText().trim();
                
                // 브랜드명 검증
                String validatedBrandName = validateBrandName(generatedBrandName, request.cropName());
                if (validatedBrandName != null) {
                    log.info("브랜드명 생성 성공 (시도 {}): cropName={}, brandName={}", 
                        attempt, request.cropName(), validatedBrandName);
                    return validatedBrandName;
                }
                
                log.warn("브랜드명 검증 실패 (시도 {}): cropName={}, 생성된 이름={}", 
                    attempt, request.cropName(), generatedBrandName);
                
            } catch (Exception e) {
                log.error("브랜드명 생성 실패 (시도 {}): cropName={}, error={}", 
                    attempt, request.cropName(), e.getMessage());
            }
        }
        
        // 모든 재시도 실패 시 Fallback
        String fallbackBrandName = generateFallbackBrandName(request.cropName(), request.brandingKeywords());
        log.error("모든 재시도 실패, Fallback 사용: cropName={}, fallbackBrandName={}", 
            request.cropName(), fallbackBrandName);
        return fallbackBrandName;
    }
    
    /**
     * 브랜드명 검증
     */
    private String validateBrandName(String brandName, String cropName) {
        if (brandName == null || brandName.trim().isEmpty()) {
            log.warn("브랜드명이 비어있음");
            return null;
        }
        
        brandName = brandName.trim();
        
        // 마크다운 형식 제거 (- **브랜드명:** 형태)
        brandName = brandName.replaceAll("^\\s*-\\s*\\*\\*브랜드명\\*\\*\\s*:\\s*", "");
        brandName = brandName.replaceAll("^\\s*-\\s*브랜드명\\s*:\\s*", "");
        brandName = brandName.replaceAll("^\\s*\\*\\*브랜드명\\*\\*\\s*:\\s*", "");
        brandName = brandName.replaceAll("^\\s*브랜드명\\s*:\\s*", "");
        
        // 불필요한 인용부호 제거
        if (brandName.startsWith("\"") && brandName.endsWith("\"")) {
            brandName = brandName.substring(1, brandName.length() - 1).trim();
        }
        if (brandName.startsWith("'") && brandName.endsWith("'")) {
            brandName = brandName.substring(1, brandName.length() - 1).trim();
        }
        
        // 마크다운 볼드 제거 (**텍스트**)
        brandName = brandName.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");
        
        // 앞뒤 특수문자 제거
        brandName = brandName.replaceAll("^[\\-\\*\\s:\"']+", "");
        brandName = brandName.replaceAll("[\\-\\*\\s:\"']+$", "");
        
        brandName = brandName.trim();
        
        if (brandName.isEmpty()) {
            log.warn("브랜드명 정리 후 비어있음");
            return null;
        }
        
        // 길이 검증 (2-20자)
        if (brandName.length() < 2 || brandName.length() > 20) {
            log.warn("브랜드명 길이 부적절: {}자 (2-20자 권장), 내용: [{}]", brandName.length(), brandName);
            return null;
        }
        
        // 단어 개수 검증 (공백 기준 1-3개 단어)
        String[] words = brandName.split("\\s+");
        if (words.length > 3) {
            log.warn("브랜드명이 너무 복잡함: {}개 단어, 내용: [{}]", words.length, brandName);
            return null;
        }
        
        // 특수문자 체크 (기본적인 한글, 영문, 숫자만 허용) - 더 관대하게
        if (!brandName.matches("^[가-힣a-zA-Z0-9\\s]+$")) {
            log.warn("브랜드명에 허용되지 않는 문자 포함: [{}]", brandName);
            return null;
        }
        
        // 설명문이나 문장 형태인지 체크
        if (brandName.contains("브랜드") || brandName.contains("이름") || 
            brandName.contains("입니다") || brandName.contains("합니다") ||
            brandName.length() > 15) {
            log.warn("브랜드명이 설명문 형태: [{}]", brandName);
            return null;
        }
        
        log.debug("브랜드명 검증 성공: [{}]", brandName);
        return brandName;
    }
    
    @Override
    public BrandingProjectResponse createBrandingProjectWithAi(BrandingProjectCreateRequest request, User currentUser, String brandName, String unused1, String unused2, String unused3) {
        // AI 브랜딩 사용량 체크 및 증가
        userService.incrementAiBrandingUsage(currentUser.getId());
        
        // 동적 정보 추출
        String cropName = request.cropName();
        String variety = request.variety() != null ? request.variety() : "";
        String cultivationMethod = request.cultivationMethod() != null ? request.cultivationMethod() : "";
        String grade = request.grade() != null ? request.grade().getKoreanName() : "";
        String location = currentUser.getLocation() != null ? currentUser.getLocation() : "";
        String gapNumber = ""; // BrandingProjectCreateRequest에 gapNumber가 없음
        String brandImageKeywords = String.join(", ", request.logoImageKeywords());
        String cropAppealKeywords = String.join(", ", request.cropAppealKeywords());

        // 농가명 포함 여부에 따른 농가명 정보 설정
        String farmName = "";
        if (request.includeFarmName() != null && request.includeFarmName() && currentUser.getFarmName() != null) {
            farmName = currentUser.getFarmName();
        }

        // 홍보 문구/스토리 프롬프트 생성
        String conceptAndStoryPrompt = String.format(
            "**🚨 중요 길이 요구사항 🚨**\n" +
            "- 홍보 문구(concept): 15-35자\n" +
            "- 판매글(story): 최소 350자 이상 (400-500자 권장)\n" +
            "**판매글이 300자 미만이면 절대 안됩니다. 반드시 350자 이상으로 작성하세요.**\n\n" +
            "작물명: %s\n품종: %s\n재배방식: %s\n등급: %s\n농가위치: %s\n농가명: %s\n브랜드명: %s\nGAP인증번호: %s\n브랜드이미지키워드: %s\n작물매력키워드: %s\n\n%s",
            cropName, variety, cultivationMethod, grade, location, farmName, brandName, gapNumber, brandImageKeywords, cropAppealKeywords,
            CONCEPT_AND_STORY_PROMPT_TEMPLATE
        );

        // 이미지 생성용 영문 프롬프트 구성
        String logoPrompt = createLogoImagePrompt(brandName, cropName, variety, brandImageKeywords, cropAppealKeywords);

        try {
            log.info("브랜딩 생성 시작: brandName={}, cropName={}", brandName, cropName);
            long startTime = System.currentTimeMillis();
            
            // 1. 로고 생성 (비동기)
            CompletableFuture<String> logoFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    log.info("로고 생성 시작: brandName={}", brandName);
                    long logoStartTime = System.currentTimeMillis();
                    
                    String logoUrl = imageGenerationService.generateBrandLogo(brandName, request.brandingKeywords(), logoPrompt);
                    
                    long logoEndTime = System.currentTimeMillis();
                    log.info("로고 생성 완료: brandName={}, 소요시간={}ms", brandName, logoEndTime - logoStartTime);
                    
                    return logoUrl;
                } catch (Exception e) {
                    log.error("로고 생성 실패: brandName={}, error={}", brandName, e.getMessage());
                    throw new RuntimeException("로고 생성 실패", e);
                }
            });

            // 2. 홍보 문구/스토리 생성 (비동기)
            CompletableFuture<String[]> conceptStoryFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    log.info("홍보 문구/스토리 생성 시작: brandName={}", brandName);
                    long conceptStartTime = System.currentTimeMillis();
                    
                    String[] conceptStory = generateConceptAndStoryWithRetry(conceptAndStoryPrompt, brandName, 3);
                    
                    long conceptEndTime = System.currentTimeMillis();
                    log.info("홍보 문구/스토리 생성 완료: brandName={}, 소요시간={}ms", brandName, conceptEndTime - conceptStartTime);
                    
                    return conceptStory;
                    
                } catch (Exception e) {
                    log.error("홍보 문구/스토리 생성 실패: brandName={}, error={}", brandName, e.getMessage());
                    // Fallback 값 반환
                    return new String[]{
                        brandName + "과 함께하는 건강한 삶",
                        "정성과 사랑으로 키운 " + brandName + "입니다. 자연 그대로의 맛과 영양을 담아, 건강한 식탁을 만들어가는 브랜드입니다. " +
                        "우리 농장은 깨끗한 환경에서 친환경적인 재배 방식을 통해 최고 품질의 농산물을 생산합니다. " +
                        "각각의 작물은 정성스럽게 관리되어 신선함과 맛을 극대화했으며, 엄격한 품질 관리를 통해 소비자에게 안전하고 건강한 먹거리를 제공합니다. " +
                        brandName + "의 특별함을 직접 경험해보세요. 자연이 선사하는 진정한 맛의 감동을 느낄 수 있을 것입니다. " +
                        "건강한 가족의 식탁을 위한 최고의 선택, " + brandName + "을 만나보세요."
                    };
                }
            });

            // 3. 두 작업 완료 대기 (개별 타임아웃 적용)
            String logoUrl;
            String[] conceptStory;
            
            try {
                // 로고 생성 대기 (최대 60초)
                logoUrl = logoFuture.get(60, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("로고 생성 타임아웃 또는 실패: brandName={}, error={}", brandName, e.getMessage());
                logoUrl = null; // 로고 없이 진행
            }
            
            try {
                // 홍보 문구/스토리 대기 (최대 15초 - 보통 5초 내 완료)
                conceptStory = conceptStoryFuture.get(15, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("홍보 문구/스토리 생성 타임아웃 또는 실패: brandName={}, error={}", brandName, e.getMessage());
                // Fallback 값 사용
                conceptStory = new String[]{
                    brandName + "과 함께하는 건강한 삶",
                    "정성과 사랑으로 키운 " + brandName + "입니다. 자연 그대로의 맛과 영양을 담아, 건강한 식탁을 만들어가는 브랜드입니다. " +
                    "우리 농장은 깨끗한 환경에서 친환경적인 재배 방식을 통해 최고 품질의 농산물을 생산합니다. " +
                    "각각의 작물은 정성스럽게 관리되어 신선함과 맛을 극대화했으며, 엄격한 품질 관리를 통해 소비자에게 안전하고 건강한 먹거리를 제공합니다. " +
                    brandName + "의 특별함을 직접 경험해보세요. 자연이 선사하는 진정한 맛의 감동을 느낄 수 있을 것입니다. " +
                    "건강한 가족의 식탁을 위한 최고의 선택, " + brandName + "을 만나보세요."
                };
            }
            
            long totalTime = System.currentTimeMillis() - startTime;
            log.info("브랜딩 생성 완료: brandName={}, 총 소요시간={}ms, logoSuccess={}", 
                brandName, totalTime, logoUrl != null);

            BrandingProject project = BrandingProject.builder()
                    .title(request.title())
                    .user(currentUser)
                    .cropName(request.cropName())
                    .variety(request.variety())
                    .cultivationMethod(request.cultivationMethod())
                    .grade(request.grade())
                    .includeFarmName(request.includeFarmName())
                    .brandingKeywords(request.brandingKeywords())
                    .cropAppealKeywords(request.cropAppealKeywords())
                    .logoImageKeywords(request.logoImageKeywords())
                    .generatedBrandName(brandName)
                    .brandImageUrl(logoUrl) // null일 수 있음
                    .brandConcept(conceptStory[0])
                    .brandStory(conceptStory[1])
                    .build();
            
            BrandingProject savedProject = brandingProjectRepository.save(project);
            log.info("AI 기반 브랜딩 프로젝트 생성 완료: projectId={}, userId={}, 총 처리시간={}ms", 
                savedProject.getId(), currentUser.getId(), totalTime);
            
            return BrandingProjectResponse.from(savedProject);
            
        } catch (Exception e) {
            log.error("AI 기반 브랜딩 프로젝트 생성 실패: brandName={}, error={}", brandName, e.getMessage(), e);
            
            // Fallback으로 기본 프로젝트 생성
            BrandingProject fallbackProject = BrandingProject.builder()
                    .title(request.title())
                    .user(currentUser)
                    .cropName(request.cropName())
                    .variety(request.variety())
                    .cultivationMethod(request.cultivationMethod())
                    .grade(request.grade())
                    .includeFarmName(request.includeFarmName())
                    .brandingKeywords(request.brandingKeywords())
                    .cropAppealKeywords(request.cropAppealKeywords())
                    .logoImageKeywords(request.logoImageKeywords())
                    .generatedBrandName(brandName)
                    .brandConcept(brandName + "과 함께하는 건강한 삶")
                    .brandStory("정성과 사랑으로 키운 " + brandName + "입니다. 자연 그대로의 맛과 영양을 담아, 건강한 식탁을 만들어가는 브랜드입니다. " +
                            "우리 농장은 깨끗한 환경에서 친환경적인 재배 방식을 통해 최고 품질의 농산물을 생산합니다. " +
                            "각각의 작물은 정성스럽게 관리되어 신선함과 맛을 극대화했으며, 엄격한 품질 관리를 통해 소비자에게 안전하고 건강한 먹거리를 제공합니다. " +
                            brandName + "의 특별함을 직접 경험해보세요. 자연이 선사하는 진정한 맛의 감동을 느낄 수 있을 것입니다. " +
                            "건강한 가족의 식탁을 위한 최고의 선택, " + brandName + "을 만나보세요.")
                    .build();
            
            BrandingProject savedProject = brandingProjectRepository.save(fallbackProject);
            return BrandingProjectResponse.from(savedProject);
        }
    }
    
    /**
     * 🚀 점진적 브랜딩 생성 (텍스트 먼저, 이미지 나중에)
     */
    @Override
    public BrandingProjectResponse createBrandingProjectProgressive(BrandingProjectCreateRequest request, User currentUser, String brandName) {
        // AI 브랜딩 사용량 체크 및 증가
        userService.incrementAiBrandingUsage(currentUser.getId());
        
        // 동적 정보 추출
        String cropName = request.cropName();
        String variety = request.variety() != null ? request.variety() : "";
        String cultivationMethod = request.cultivationMethod() != null ? request.cultivationMethod() : "";
        String grade = request.grade() != null ? request.grade().getKoreanName() : "";
        String location = currentUser.getLocation() != null ? currentUser.getLocation() : "";
        String gapNumber = "";
        String brandImageKeywords = String.join(", ", request.logoImageKeywords());
        String cropAppealKeywords = String.join(", ", request.cropAppealKeywords());

        // 농가명 포함 여부에 따른 농가명 정보 설정
        String farmName = "";
        if (request.includeFarmName() != null && request.includeFarmName() && currentUser.getFarmName() != null) {
            farmName = currentUser.getFarmName();
        }

        // 키워드 전달 상태 로깅
        log.info("점진적 브랜딩 요청 키워드 확인:");
        log.info("- brandingKeywords: {}", request.brandingKeywords());
        log.info("- cropAppealKeywords: {}", request.cropAppealKeywords());
        log.info("- logoImageKeywords: {}", request.logoImageKeywords());
        log.info("- 조합된 brandImageKeywords: [{}]", brandImageKeywords);
        log.info("- 조합된 cropAppealKeywords: [{}]", cropAppealKeywords);
        log.info("- 농가명 포함 여부: {}, 농가명: [{}]", request.includeFarmName(), farmName);

        // 홍보 문구/스토리 프롬프트 생성
        String conceptAndStoryPrompt = String.format(
            "**🚨 중요 길이 요구사항 🚨**\n" +
            "- 홍보 문구(concept): 15-35자\n" +
            "- 판매글(story): 최소 350자 이상 (400-500자 권장)\n" +
            "**판매글이 300자 미만이면 절대 안됩니다. 반드시 350자 이상으로 작성하세요.**\n\n" +
            "작물명: %s\n품종: %s\n재배방식: %s\n등급: %s\n농가위치: %s\n농가명: %s\n브랜드명: %s\nGAP인증번호: %s\n브랜드이미지키워드: %s\n작물매력키워드: %s\n\n%s",
            cropName, variety, cultivationMethod, grade, location, farmName, brandName, gapNumber, brandImageKeywords, cropAppealKeywords,
            CONCEPT_AND_STORY_PROMPT_TEMPLATE
        );

        try {
            log.info("점진적 브랜딩 생성 시작: brandName={}, cropName={}", brandName, cropName);
            long startTime = System.currentTimeMillis();
            
            // STEP 1: 텍스트 먼저 생성 (동기 처리)
            log.info("홍보 문구/스토리 생성 시작: brandName={}", brandName);
            long conceptStartTime = System.currentTimeMillis();
            
            String[] conceptStory = generateConceptAndStoryWithRetry(conceptAndStoryPrompt, brandName, 3);
            
            long conceptEndTime = System.currentTimeMillis();
            log.info("홍보 문구/스토리 생성 완료: brandName={}, 소요시간={}ms", brandName, conceptEndTime - conceptStartTime);
            
            // STEP 2: 텍스트와 함께 프로젝트 즉시 저장 (이미지 상태: PROCESSING)
            BrandingProject project = BrandingProject.builder()
                    .title(request.title())
                    .user(currentUser)
                    .cropName(request.cropName())
                    .variety(request.variety())
                    .cultivationMethod(request.cultivationMethod())
                    .grade(request.grade())
                    .includeFarmName(request.includeFarmName())
                    .brandingKeywords(request.brandingKeywords())
                    .cropAppealKeywords(request.cropAppealKeywords())
                    .logoImageKeywords(request.logoImageKeywords())
                    .generatedBrandName(brandName)
                    .brandImageUrl(null) // 이미지 아직 없음
                    .brandConcept(conceptStory[0])
                    .brandStory(conceptStory[1])
                    .imageGenerationStatus(ImageGenerationStatus.PROCESSING) // 처리 중
                    .build();
            
            BrandingProject savedProject = brandingProjectRepository.save(project);
            
            long textTime = System.currentTimeMillis() - startTime;
            log.info("텍스트 브랜딩 완료, 즉시 반환: projectId={}, 텍스트 처리시간={}ms", 
                savedProject.getId(), textTime);
            
            // STEP 3: 백그라운드에서 이미지 생성 (비동기)
            String logoPrompt = createLogoImagePrompt(brandName, cropName, variety, brandImageKeywords, cropAppealKeywords);
            
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("백그라운드 로고 생성 시작: projectId={}, brandName={}", savedProject.getId(), brandName);
                    long logoStartTime = System.currentTimeMillis();
                    
                    String logoUrl = imageGenerationService.generateBrandLogo(brandName, request.brandingKeywords(), logoPrompt);
                    
                    // 이미지 생성 완료 후 DB 업데이트
                    BrandingProject projectToUpdate = brandingProjectRepository.findById(savedProject.getId())
                            .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다: " + savedProject.getId()));
                    
                    projectToUpdate.completeImageGeneration(logoUrl);
                    brandingProjectRepository.save(projectToUpdate);
                    
                    long logoEndTime = System.currentTimeMillis();
                    log.info("백그라운드 로고 생성 완료: projectId={}, 로고 처리시간={}ms", 
                        savedProject.getId(), logoEndTime - logoStartTime);
                    
                } catch (Exception e) {
                    log.error("백그라운드 로고 생성 실패: projectId={}, brandName={}, error={}", 
                        savedProject.getId(), brandName, e.getMessage());
                    
                    // 실패 시 상태 업데이트
                    try {
                        BrandingProject projectToUpdate = brandingProjectRepository.findById(savedProject.getId())
                                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다: " + savedProject.getId()));
                        
                        projectToUpdate.failImageGeneration();
                        brandingProjectRepository.save(projectToUpdate);
                    } catch (Exception updateError) {
                        log.error("이미지 실패 상태 업데이트 실패: {}", updateError.getMessage());
                    }
                }
            });
            
            // 텍스트 정보로 즉시 응답 반환
            return BrandingProjectResponse.from(savedProject);
            
        } catch (Exception e) {
            log.error("점진적 브랜딩 생성 실패: brandName={}, error={}", brandName, e.getMessage(), e);
            
            // Fallback으로 기본 프로젝트 생성
            BrandingProject fallbackProject = BrandingProject.builder()
                    .title(request.title())
                    .user(currentUser)
                    .cropName(request.cropName())
                    .variety(request.variety())
                    .cultivationMethod(request.cultivationMethod())
                    .grade(request.grade())
                    .includeFarmName(request.includeFarmName())
                    .brandingKeywords(request.brandingKeywords())
                    .cropAppealKeywords(request.cropAppealKeywords())
                    .logoImageKeywords(request.logoImageKeywords())
                    .generatedBrandName(brandName)
                    .brandConcept(brandName + "과 함께하는 건강한 삶")
                    .brandStory("정성과 사랑으로 키운 " + brandName + "입니다. 자연 그대로의 맛과 영양을 담아, 건강한 식탁을 만들어가는 브랜드입니다. " +
                            "우리 농장은 깨끗한 환경에서 친환경적인 재배 방식을 통해 최고 품질의 농산물을 생산합니다. " +
                            "각각의 작물은 정성스럽게 관리되어 신선함과 맛을 극대화했으며, 엄격한 품질 관리를 통해 소비자에게 안전하고 건강한 먹거리를 제공합니다. " +
                            brandName + "의 특별함을 직접 경험해보세요. 자연이 선사하는 진정한 맛의 감동을 느낄 수 있을 것입니다. " +
                            "건강한 가족의 식탁을 위한 최고의 선택, " + brandName + "을 만나보세요.")
                    .imageGenerationStatus(ImageGenerationStatus.FAILED)
                    .build();
            
            BrandingProject savedProject = brandingProjectRepository.save(fallbackProject);
            return BrandingProjectResponse.from(savedProject);
        }
    }
    
    /**
     * 로고 이미지 생성을 위한 프롬프트 생성 (새로운 템플릿 기반)
     */
    private String createLogoImagePrompt(String brandName, String cropName, String variety, String brandImageKeywords, String cropAppealKeywords) {
        log.info("로고 프롬프트 생성 - brandName: {}, cropName: {}, variety: {}", brandName, cropName, variety);
        log.info("키워드 정보 - brandImageKeywords: [{}], cropAppealKeywords: [{}]", brandImageKeywords, cropAppealKeywords);
        
        // 키워드 조합 (브랜드 이미지 + 작물 매력)
        String combinedKeywords = "";
        if (brandImageKeywords != null && !brandImageKeywords.trim().isEmpty() && 
            cropAppealKeywords != null && !cropAppealKeywords.trim().isEmpty()) {
            combinedKeywords = brandImageKeywords + ", " + cropAppealKeywords;
        } else if (brandImageKeywords != null && !brandImageKeywords.trim().isEmpty()) {
            combinedKeywords = brandImageKeywords;
        } else if (cropAppealKeywords != null && !cropAppealKeywords.trim().isEmpty()) {
            combinedKeywords = cropAppealKeywords;
        } else {
            log.warn("브랜드 이미지 키워드와 작물 매력 키워드가 모두 비어있습니다. 기본값을 사용합니다.");
            combinedKeywords = "professional, fresh, quality, natural, modern";
        }
        
        log.info("최종 조합된 키워드: [{}]", combinedKeywords);
        
        String finalPrompt = LOGO_PROMPT_TEMPLATE
                .replace("{brandName}", brandName)
                .replace("{cropName}", cropName)
                .replace("{keywords}", combinedKeywords);
                
        log.debug("생성된 로고 프롬프트: {}", finalPrompt);
        
        return finalPrompt;
    }
    
    /**
     * 홍보 문구/스토리 생성 (재시도 로직 포함) - JSON 방식
     */
    private String[] generateConceptAndStoryWithRetry(String prompt, String brandName, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("홍보 문구/스토리 생성 시도 {}/{}: brandName={}", attempt, maxRetries, brandName);
                
                ChatResponse conceptResponse = chatModel.call(
                    new Prompt(prompt, OpenAiChatOptions.builder()
                        .model("gpt-4o-mini")
                        .maxTokens(1500) // 더 긴 응답을 위해 토큰 수 증가
                        .temperature(0.7) // 창의성을 위해 온도 조정
                        .build())
                );
                
                String fullResponse = conceptResponse.getResult().getOutput().getText().trim();
                log.debug("GPT JSON 응답 (시도 {}): {}", attempt, fullResponse);
                
                // JSON 응답 검증 및 파싱
                String[] result = parseJsonResponse(fullResponse, brandName);
                if (result != null) {
                    log.info("홍보 문구/스토리 생성 성공 (시도 {}): brandName={}", attempt, brandName);
                    return result;
                }
                
                log.warn("JSON 응답 형식이 올바르지 않음 (시도 {}): brandName={}", attempt, brandName);
                
            } catch (Exception e) {
                log.error("홍보 문구/스토리 생성 실패 (시도 {}): brandName={}, error={}", 
                    attempt, brandName, e.getMessage());
            }
        }
        
        log.error("모든 재시도 실패, Fallback 사용: brandName={}", brandName);
        return new String[]{
            brandName + "과 함께하는 건강한 삶",
            "정성과 사랑으로 키운 " + brandName + "입니다. 자연 그대로의 맛과 영양을 담아, 건강한 식탁을 만들어가는 브랜드입니다. " +
            "우리 농장은 깨끗한 환경에서 친환경적인 재배 방식을 통해 최고 품질의 농산물을 생산합니다. " +
            "각각의 작물은 정성스럽게 관리되어 신선함과 맛을 극대화했으며, 엄격한 품질 관리를 통해 소비자에게 안전하고 건강한 먹거리를 제공합니다. " +
            brandName + "의 특별함을 직접 경험해보세요. 자연이 선사하는 진정한 맛의 감동을 느낄 수 있을 것입니다. " +
            "건강한 가족의 식탁을 위한 최고의 선택, " + brandName + "을 만나보세요."
        };
    }
    
    /**
     * JSON 응답 파싱 및 검증
     */
    private String[] parseJsonResponse(String jsonResponse, String brandName) {
        try {
            // JSON 블록 추출 (```json ... ``` 형태일 수도 있음)
            String cleanJson = jsonResponse;
            if (jsonResponse.contains("```json")) {
                int start = jsonResponse.indexOf("```json") + 7;
                int end = jsonResponse.lastIndexOf("```");
                if (start > 6 && end > start) {
                    cleanJson = jsonResponse.substring(start, end).trim();
                }
            } else if (jsonResponse.contains("```")) {
                int start = jsonResponse.indexOf("```") + 3;
                int end = jsonResponse.lastIndexOf("```");
                if (start > 2 && end > start) {
                    cleanJson = jsonResponse.substring(start, end).trim();
                }
            }
            
            // JSON 시작과 끝 찾기
            int jsonStart = cleanJson.indexOf("{");
            int jsonEnd = cleanJson.lastIndexOf("}");
            
            if (jsonStart == -1 || jsonEnd == -1 || jsonStart >= jsonEnd) {
                log.warn("JSON 구조를 찾을 수 없음: {}", cleanJson.substring(0, Math.min(100, cleanJson.length())));
                return null;
            }
            
            cleanJson = cleanJson.substring(jsonStart, jsonEnd + 1);
            log.debug("정제된 JSON: {}", cleanJson);
            
            // 간단한 JSON 파싱 (Jackson 없이)
            String concept = extractJsonValue(cleanJson, "concept");
            String story = extractJsonValue(cleanJson, "story");
            
            if (concept == null || story == null) {
                log.warn("JSON 파싱 실패: concept={}, story={}", concept != null, story != null);
                return null;
            }
            
            // 검증
            if (!validateConceptAndStory(concept, story, brandName)) {
                return null;
            }
            
            log.info("JSON 응답 파싱 및 검증 성공: 홍보문구={}자, 판매글={}자", concept.length(), story.length());
            log.debug("홍보문구: [{}]", concept);
            log.debug("판매글: [{}]", story.substring(0, Math.min(100, story.length())) + "...");
            
            return new String[]{concept, story};
            
        } catch (Exception e) {
            log.error("JSON 응답 파싱 중 오류: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 간단한 JSON 값 추출
     */
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*(?:\\\\.[^\"]*)*)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher m = p.matcher(json);
            
            if (m.find()) {
                String value = m.group(1);
                // 이스케이프 문자 처리
                value = value.replace("\\\"", "\"")
                           .replace("\\n", "\n")
                           .replace("\\t", "\t")
                           .replace("\\\\", "\\");
                return value.trim();
            }
            return null;
        } catch (Exception e) {
            log.error("JSON 값 추출 실패: key={}, error={}", key, e.getMessage());
            return null;
        }
    }
    
    /**
     * 홍보문구와 판매글 검증
     */
    private boolean validateConceptAndStory(String concept, String story, String brandName) {
        // 홍보 문구 검증 (10-40자로 완화, 기존 15-35자에서 범위 확대)
        if (concept.length() < 10 || concept.length() > 40) {
            log.warn("홍보 문구 길이 부적절: {}자 (10-40자 권장), 내용: [{}]", concept.length(), concept);
            return false;
        }
        
        // 명사로 끝나는지 검증 (조건 완화: 경고만 하고 통과)
        if (concept.endsWith("다") || concept.endsWith("요") || 
            concept.endsWith("니다") || concept.endsWith("습니다")) {
            log.warn("홍보 문구가 동사/형용사로 끝남: [{}] - 하지만 허용", concept);
            // 경고만 하고 통과시킴
        }
        
        // 판매 글 검증 (300-800자로 범위 확대, 기존 350-600자에서 완화)
        if (story.length() < 300) {
            log.warn("판매 글이 너무 짧습니다: {}자 (최소 300자 권장), 내용: [{}]", 
                story.length(), story.substring(0, Math.min(100, story.length())));
            return false;
        }
        
        if (story.length() > 800) {
            log.warn("판매 글이 너무 깁니다: {}자 (최대 800자 권장), 내용: [{}]", 
                story.length(), story.substring(0, Math.min(100, story.length())));
            // 너무 길어도 허용 (경고만)
        }
        
        // 브랜드명이 홍보 문구에 포함되어 있는지 체크 (조건 완화: 경고만)
        if (concept.contains(brandName)) {
            log.warn("홍보 문구에 브랜드명이 포함됨: [{}] contains [{}] - 하지만 허용", concept, brandName);
            // 경고만 하고 통과시킴
        }
        
        // 내용이 너무 단순한지 체크 (조건 완화)
        if (concept.equals(story)) {
            log.warn("홍보 문구와 판매 글이 동일함 - 실패");
            return false;
        }
        
        if (concept.length() >= story.length()) {
            log.warn("홍보 문구가 판매 글보다 김: conceptLen={}, storyLen={} - 하지만 허용", 
                concept.length(), story.length());
            // 경고만 하고 통과시킴
        }
        
        log.info("홍보문구/판매글 검증 통과: 홍보문구={}자, 판매글={}자", concept.length(), story.length());
        return true;
    }
    
    /**
     * Fallback 브랜드명 생성
     */
    private String generateFallbackBrandName(String cropName, List<String> brandingKeywords) {
        // 키워드 기반 브랜드명 생성 시도
        if (brandingKeywords != null && !brandingKeywords.isEmpty()) {
            for (String keyword : brandingKeywords) {
                if (keyword.length() <= 3) { // 짧은 키워드만 사용
                    return keyword + cropName;
                }
            }
        }
        
        // 기본 패턴들
        String[] patterns = {
            cropName + "원",     // 토마토원
            "신선" + cropName,   // 신선토마토  
            cropName + "팜",     // 토마토팜
            "자연" + cropName,   // 자연토마토
            cropName + "랜드"    // 토마토랜드
        };
        
        // 작물명 길이에 따라 적절한 패턴 선택
        if (cropName.length() <= 2) {
            return patterns[0]; // 짧은 작물명엔 "원" 붙이기
        } else {
            return patterns[1]; // 긴 작물명엔 "신선" 앞에 붙이기
        }
    }
    
    private BrandingProject findProjectByIdAndUser(Long projectId, Long userId) {
        return brandingProjectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.USER_NOT_FOUND));
    }
} 