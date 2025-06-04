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
        "사용자가 제공한 정보를 활용하여 홍보 문구와 판매 글을 생성하세요.\n" +
        "\n" +
        "## 📋 생성 규칙\n" +
        "### 홍보 문구 (반드시 지켜주세요)\n" +
        "- **길이**: 15자 이상 40자 이하\n" +
        "- **형식**: 한 줄로 간결하게\n" +
        "- **내용**: 브랜드의 핵심 가치를 담은 캐치프레이즈\n" +
        "- **어조**: 임팩트 있고 기억에 남는 문구\n" +
        "- **예시**: \"달콤함이 터지는 프리미엄 토마토\", \"자연이 키운 건강한 맛\"\n" +
        "\n" +
        "### 판매 글 (반드시 지켜주세요)\n" +
        "- **길이**: 200자 이상 500자 이하\n" +
        "- **내용**: 구체적인 스토리, 재배 과정, 품질, 차별점 포함\n" +
        "- **구성**: 농장 소개 → 재배 과정 → 품질/맛 → 구매 유도\n" +
        "\n" +
        "## 💡 성공 예시\n" +
        "홍보 문구: 달콤함과 건강을 담은 미래호빵의 아삭호빵\n" +
        "판매 글: 경기도 화성시 동탄면에서 자란 쫑마를 스파이시 큐트는 특등급의 마늘로, 노지에서 자연의 힘을 온전히 받아 성장했습니다. 따뜻하고 귀여운 브랜드 이미지에 걸맞게, 이 마늘은 풍부한 수분과 매콤한 맛으로 요리의 풍미를 한층 높여줍니다. 쫑마를은 그 자체로도 뛰어난 맛을 자랑하지만, 각종 요리에 활용하기에 최적의 선택입니다. 요리에 깊이를 더하고 싶다면, 쫑마를 스파이시 큐트를 추천합니다.\n" +
        "\n" +
        "## ⚠️ 주의사항\n" +
        "- 홍보 문구는 절대 40자를 넘으면 안 됩니다\n" +
        "- 판매 글은 반드시 200자 이상 작성해주세요\n" +
        "- 반드시 아래 형식으로만 답변하세요\n" +
        "\n" +
        "## 📤 출력 형식 (정확히 이 형식으로만 답변)\n" +
        "홍보 문구: [15-40자 이내의 홍보 문구]\n" +
        "판매 글: [200-500자 이내의 상세한 판매 글]";

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
                    
                    String[] conceptStory = generateConceptAndStoryWithRetry(conceptAndStoryPrompt, brandName, 3);
                    
                    long conceptEndTime = System.currentTimeMillis();
                    log.info("홍보 문구/스토리 생성 완료: brandName={}, 소요시간={}ms", brandName, conceptEndTime - conceptStartTime);
                    
                    return conceptStory;
                    
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

        // 키워드 전달 상태 로깅
        log.info("점진적 브랜딩 요청 키워드 확인:");
        log.info("- brandingKeywords: {}", request.brandingKeywords());
        log.info("- cropAppealKeywords: {}", request.cropAppealKeywords());
        log.info("- logoImageKeywords: {}", request.logoImageKeywords());
        log.info("- 조합된 brandImageKeywords: [{}]", brandImageKeywords);
        log.info("- 조합된 cropAppealKeywords: [{}]", cropAppealKeywords);

        // 홍보 문구/스토리 프롬프트 생성
        String conceptAndStoryPrompt = String.format(
            "작물명: %s\n품종: %s\n재배방식: %s\n등급: %s\n농가위치: %s\n브랜드명: %s\nGAP인증번호: %s\n브랜드이미지키워드: %s\n작물매력키워드: %s\n\n%s",
            cropName, variety, cultivationMethod, grade, location, brandName, gapNumber, brandImageKeywords, cropAppealKeywords,
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
                    .brandingKeywords(request.brandingKeywords())
                    .cropAppealKeywords(request.cropAppealKeywords())
                    .logoImageKeywords(request.logoImageKeywords())
                    .generatedBrandName(brandName)
                    .brandConcept(brandName + "과 함께하는 건강한 삶")
                    .brandStory("정성과 사랑으로 키운 " + brandName + "입니다. 자연 그대로의 맛과 영양을 담아, 건강한 식탁을 만들어가는 브랜드입니다.")
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
     * 홍보 문구/스토리 생성 (재시도 로직 포함)
     */
    private String[] generateConceptAndStoryWithRetry(String prompt, String brandName, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("홍보 문구/스토리 생성 시도 {}/{}: brandName={}", attempt, maxRetries, brandName);
                
                ChatResponse conceptResponse = chatModel.call(
                    new Prompt(prompt, OpenAiChatOptions.builder()
                        .model("gpt-4o-mini")
                        .maxTokens(1000)
                        .temperature(0.7) // 일관성을 위해 온도 조금 낮춤
                        .build())
                );
                
                String fullResponse = conceptResponse.getResult().getOutput().getText().trim();
                log.debug("GPT 응답 (시도 {}): {}", attempt, fullResponse);
                
                // 응답 검증 및 파싱
                String[] result = parseAndValidateResponse(fullResponse, brandName);
                if (result != null) {
                    log.info("홍보 문구/스토리 생성 성공 (시도 {}): brandName={}", attempt, brandName);
                    return result;
                }
                
                log.warn("응답 형식이 올바르지 않음 (시도 {}): brandName={}", attempt, brandName);
                
            } catch (Exception e) {
                log.error("홍보 문구/스토리 생성 실패 (시도 {}): brandName={}, error={}", 
                    attempt, brandName, e.getMessage());
            }
        }
        
        log.error("모든 재시도 실패, Fallback 사용: brandName={}", brandName);
        return new String[]{
            brandName + "과 함께하는 건강한 삶",
            "정성과 사랑으로 키운 " + brandName + "입니다. 자연 그대로의 맛과 영양을 담아, 건강한 식탁을 만들어가는 브랜드입니다. 우리의 정직한 재배 방식과 깐깐한 품질 관리를 통해 최고의 맛과 영양을 선사합니다."
        };
    }
    
    /**
     * GPT 응답 파싱 및 검증
     */
    private String[] parseAndValidateResponse(String fullResponse, String brandName) {
        try {
            // 정규식을 이용한 강력한 파싱
            java.util.regex.Pattern conceptPattern = java.util.regex.Pattern.compile(
                "홍보\\s*문구\\s*[:：]\\s*(.+?)(?=\\n|판매)", 
                java.util.regex.Pattern.DOTALL
            );
            java.util.regex.Pattern storyPattern = java.util.regex.Pattern.compile(
                "판매\\s*글\\s*[:：]\\s*(.+?)$", 
                java.util.regex.Pattern.DOTALL
            );
            
            java.util.regex.Matcher conceptMatcher = conceptPattern.matcher(fullResponse);
            java.util.regex.Matcher storyMatcher = storyPattern.matcher(fullResponse);
            
            if (conceptMatcher.find() && storyMatcher.find()) {
                String concept = conceptMatcher.group(1).trim();
                String story = storyMatcher.group(1).trim();
                
                // 길이 검증
                if (concept.length() < 10 || concept.length() > 50) {
                    log.warn("홍보 문구 길이 부적절: {}자 (10-50자 권장)", concept.length());
                    return null;
                }
                
                if (story.length() < 100 || story.length() > 600) {
                    log.warn("판매 글 길이 부적절: {}자 (100-600자 권장)", story.length());
                    return null;
                }
                
                // 내용 검증 (너무 간단하거나 중복된 내용 체크)
                if (concept.equals(story) || concept.length() > story.length()) {
                    log.warn("홍보 문구와 판매 글이 비정상적: concept={}, story={}", 
                        concept.length(), story.length());
                    return null;
                }
                
                log.info("응답 파싱 성공: 홍보문구={}자, 판매글={}자", concept.length(), story.length());
                return new String[]{concept, story};
            }
            
            log.warn("정규식 매칭 실패: {}", fullResponse.substring(0, Math.min(100, fullResponse.length())));
            return null;
            
        } catch (Exception e) {
            log.error("응답 파싱 중 오류: {}", e.getMessage());
            return null;
        }
    }
    
    private BrandingProject findProjectByIdAndUser(Long projectId, Long userId) {
        return brandingProjectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.USER_NOT_FOUND));
    }
} 