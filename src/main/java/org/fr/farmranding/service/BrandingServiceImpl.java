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
        "사용자로부터 정보를 수집하여 사용자 지정 작물에 적합한 브랜드명을 생성하세요. 다음과 같은 질문을 통해 사용자로부터 수집한 정보를 바탕으로 브랜드명을 생성하세요.\\n" +
        "\\n" +
        "정보:\\n" +
        "- 작물명: {cropName}\\n" +
        "- 품종: {variety}\\n" +
        "- 브랜드 이미지 키워드: {brandingKeywords}\\n" +
        "- 작물의 매력 키워드: {cropAppealKeywords}\\n" +
        "\\n" +
        "# 결과물에 사용될 정보\\n" +
        "1. 작물명, 품종\\n" +
        "2. 사용자가 원하는 브랜드 이미지\\n" +
        "3. 작물이 가진 매력\\n" +
        "\\n" +
        "# 생성 시 주의사항 및 조건\\n" +
        "- 사용자가 제공한 정보 중 작물명, 품종, 브랜드 이미지 키워드, 작물의 매력 키워드를 기반으로 적합한 브랜드명을 생성하세요.\\n" +
        "- 짧고 기억에 남을 수 있는 단어 형태로 생성하세요.\\n" +
        "- 단어를 2개 내외로 조합하여 작성하세요.\\n" +
        "- 사용자로부터 얻은 정보 중 다른 농가와 차별화될 만한 요소, 혹은 소비자에게 더 매력적으로 다가갈 것이라 판단되는 요소를 분석 및 선별하여 브랜드명을 생성합니다.\\n" +
        "- 농가 위치와 작물을 조합하거나 키워드에서 얻은 정보로 조합하는 등 참신하지만 읽었을 때 거부감 없는 브랜드명을 생성하세요.\\n" +
        "    - 예: 무등산 꿀수박 (농가 위치: 무등산, 작물의 매력: 고당도)\\n" +
        "\\n" +
        "브랜드명만 간단히 응답해주세요.";

    private static final String CONCEPT_AND_STORY_PROMPT_TEMPLATE =
        "# 🔽지령\n" +
        "사용자가 제공한 정보를 활용하여 홍보 문구와 판매 글(500자 이내)을 생성하세요. 작물의 브랜드 아이덴티티를 강조하는 문구를 작성하고 작물의 매력을 돋보이게 하세요.\n" +
        "\n" +
        "질문을 통해 필요한 정보를 수집합니다:\n" +
        "\n" +
        "1. 작물명, 품종, 재배 방식, 등급, 농가 위치\n" +
        "2. GAP 인증 작물 여부 및 인증번호\n" +
        "3. 사용자가 원하는 브랜드 이미지\n" +
        "4. 작물이 가진 매력\n" +
        "\n" +
        "# Steps\n" +
        "\n" +
        "1. **정보 수집**: 위의 질문을 통해 사용자가 제공하는 정보를 얻습니다.\n" +
        "2. **홍보 문구 작성**:\n" +
        "    - 작물명, 품종, 재배 방식, 등급, 농가 위치, 브랜드 이미지 키워드, 작물의 매력을 기반으로 한 줄 분량의 매력적인 홍보 문구를 작성하세요.\n" +
        "    - 홍보 문구는 작물의 특별함과 특성을 강조하고 명사로 끝나야 합니다.\n" +
        "    - 브랜드명과 어울리는 맥락을 유지합니다. 브랜드명과의 일관성을 최우선으로 하세요.\n" +
        "    - 홍보 문구는 명사로 끝나야 합니다.\n" +
        "3. **판매 글 작성**:\n" +
        "    - 위의 정보를 종합하여 500자 이내의 판매 글을 작성하세요.\n" +
        "    - 작물의 의미 있는 스토리를 강조하고, 수집한 모든 정보를 포함하도록 합니다.\n" +
        "\n" +
        "# Output Format\n" +
        "\n" +
        "홍보 문구: [단일 줄 홍보 문구]\n" +
        "판매 글: [500자 이내의 판매 글]";

    private static final String LOGO_PROMPT_TEMPLATE =
        "Create a professional agricultural logo design for '{brandName}' brand. The logo should include:\\n" +
        "- A clean illustration of {cropName} as the main visual element\\n" +
        "- The brand name '{brandName}' prominently displayed with an appropriate, harmonious font style that complements the overall design\\n" +
        "- Keywords represented: {keywords}\\n" +
        "- Clean vector style with modern typography\\n" +
        "- Vibrant, fresh colors that convey quality and freshness\\n" +
        "- Professional layout suitable for agricultural branding\\n" +
        "- The font style should be readable, modern, and match the agricultural theme\\n" +
        "- Ensure the text and graphics work together harmoniously\\n" +
        "- Size: 1024x1024 pixels, high quality\\n" +
        "Background: clean white or transparent";
    
    @Override
    public BrandingProjectResponse createBrandingProject(BrandingProjectCreateRequest request, User currentUser) {
        // AI 브랜딩 사용량 체크
        userService.incrementAiBrandingUsage(currentUser.getId());
        
        BrandingProject project = BrandingProject.builder()
                .title(request.title())
                .user(currentUser)
                .cropName(request.cropName())
                .variety(request.variety())
                .cultivationMethod(request.cultivationMethod())
                .grade(request.grade())
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
        
        try {
            // ChatModel을 사용한 브랜드명 생성
            ChatResponse response = chatModel.call(
                new Prompt(brandNamePrompt, OpenAiChatOptions.builder()
                    .model("gpt-4o-mini")
                    .maxTokens(50)
                    .temperature(0.7)
                    .build())
            );
            
            String generatedBrandName = response.getResult().getOutput().getText().trim();
            
            log.info("브랜드명 생성 완료: cropName={}, brandName={}", request.cropName(), generatedBrandName);
            return generatedBrandName;
            
        } catch (Exception e) {
            log.error("브랜드명 생성 실패: cropName={}, error={}", request.cropName(), e.getMessage(), e);
            throw new BusinessException(FarmrandingResponseCode.AI_SERVICE_ERROR);
        }
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

        // 홍보 문구/스토리 프롬프트 생성
        String conceptAndStoryPrompt = String.format(
            "작물명: %s\n품종: %s\n재배방식: %s\n등급: %s\n농가위치: %s\n브랜드명: %s\nGAP인증번호: %s\n브랜드이미지키워드: %s\n작물매력키워드: %s\n\n%s",
            cropName, variety, cultivationMethod, grade, location, brandName, gapNumber, brandImageKeywords, cropAppealKeywords,
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
                    
                    ChatResponse conceptResponse = chatModel.call(
                        new Prompt(conceptAndStoryPrompt, OpenAiChatOptions.builder()
                            .model("gpt-4o-mini")
                            .maxTokens(1000)
                            .temperature(0.8)
                            .build())
                    );
                    
                    String fullResponse = conceptResponse.getResult().getOutput().getText();
                    
                    // 응답 파싱 (홍보 문구/판매 글 분리)
                    String concept = "";
                    String story = "";
                    
                    if (fullResponse.contains("홍보 문구:") && fullResponse.contains("판매 글:")) {
                        try {
                            int conceptStart = fullResponse.indexOf("홍보 문구:") + 6;
                            int storyStart = fullResponse.indexOf("판매 글:");
                            
                            if (conceptStart > 5 && storyStart > conceptStart) {
                                concept = fullResponse.substring(conceptStart, storyStart).trim();
                                story = fullResponse.substring(storyStart + 5).trim();
                            } else {
                                concept = fullResponse.trim();
                                story = "정성과 사랑으로 키운 " + brandName + "입니다.";
                            }
                        } catch (Exception e) {
                            log.warn("응답 파싱 실패, 기본값 사용: {}", e.getMessage());
                            concept = fullResponse.trim();
                            story = "정성과 사랑으로 키운 " + brandName + "입니다.";
                        }
                    } else {
                        concept = fullResponse.trim();
                        story = "정성과 사랑으로 키운 " + brandName + "입니다.";
                    }
                    
                    long conceptEndTime = System.currentTimeMillis();
                    log.info("홍보 문구/스토리 생성 완료: brandName={}, 소요시간={}ms", brandName, conceptEndTime - conceptStartTime);
                    
                    return new String[]{concept, story};
                    
                } catch (Exception e) {
                    log.error("홍보 문구/스토리 생성 실패: brandName={}, error={}", brandName, e.getMessage());
                    // Fallback 값 반환
                    return new String[]{
                        brandName + "과 함께하는 건강한 삶",
                        "정성과 사랑으로 키운 " + brandName + "입니다. 자연 그대로의 맛과 영양을 담아, 건강한 식탁을 만들어가는 브랜드입니다."
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
                    "정성과 사랑으로 키운 " + brandName + "입니다. 자연 그대로의 맛과 영양을 담아, 건강한 식탁을 만들어가는 브랜드입니다."
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
                    .brandingKeywords(request.brandingKeywords())
                    .cropAppealKeywords(request.cropAppealKeywords())
                    .logoImageKeywords(request.logoImageKeywords())
                    .generatedBrandName(brandName)
                    .brandConcept(brandName + "과 함께하는 건강한 삶")
                    .brandStory("정성과 사랑으로 키운 " + brandName + "입니다. 자연 그대로의 맛과 영양을 담아, 건강한 식탁을 만들어가는 브랜드입니다.")
                    .build();
            
            BrandingProject savedProject = brandingProjectRepository.save(fallbackProject);
            return BrandingProjectResponse.from(savedProject);
        }
    }
    
    /**
     * 로고 이미지 생성을 위한 프롬프트 생성 (새로운 템플릿 기반)
     */
    private String createLogoImagePrompt(String brandName, String cropName, String variety, String brandImageKeywords, String cropAppealKeywords) {
        // 키워드 조합 (브랜드 이미지 + 작물 매력)
        String combinedKeywords = "";
        if (!brandImageKeywords.isEmpty() && !cropAppealKeywords.isEmpty()) {
            combinedKeywords = brandImageKeywords + ", " + cropAppealKeywords;
        } else if (!brandImageKeywords.isEmpty()) {
            combinedKeywords = brandImageKeywords;
        } else if (!cropAppealKeywords.isEmpty()) {
            combinedKeywords = cropAppealKeywords;
        } else {
            combinedKeywords = "professional, fresh, quality";
        }
        
        return LOGO_PROMPT_TEMPLATE
                .replace("{brandName}", brandName)
                .replace("{cropName}", cropName)
                .replace("{keywords}", combinedKeywords);
    }
    
    private BrandingProject findProjectByIdAndUser(Long projectId, Long userId) {
        return brandingProjectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.USER_NOT_FOUND));
    }
} 