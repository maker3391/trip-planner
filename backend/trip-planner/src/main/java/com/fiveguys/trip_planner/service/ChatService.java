package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.client.OpenAiClient;
import com.fiveguys.trip_planner.dto.ChatRequest;
import com.fiveguys.trip_planner.dto.DayPlanDraft;
import com.fiveguys.trip_planner.dto.RecommendationDraft;
import com.fiveguys.trip_planner.dto.RecommendationItemDraft;
import com.fiveguys.trip_planner.response.ChatResponse;
import com.fiveguys.trip_planner.response.DayPlanResponse;
import com.fiveguys.trip_planner.response.RecommendationContentResponse;
import com.fiveguys.trip_planner.response.RecommendationItemResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final OpenAiClient openAiClient;
    private final RecommendationIntentResolverService intentResolverService;
    private final KakaoPlaceRecommendationService kakaoPlaceRecommendationService;
    private final RecommendationValidationService validationService;
    private final RecommendationNormalizationService normalizationService;
    private final RecommendationQualityService qualityService;
    private final RecommendationCacheService recommendationCacheService;
    private final RecommendationCacheKeyGenerator cacheKeyGenerator;

    public ChatService(OpenAiClient openAiClient,
                       RecommendationIntentResolverService intentResolverService,
                       KakaoPlaceRecommendationService kakaoPlaceRecommendationService,
                       RecommendationValidationService validationService,
                       RecommendationNormalizationService normalizationService,
                       RecommendationQualityService qualityService,
                       RecommendationCacheService recommendationCacheService,
                       RecommendationCacheKeyGenerator cacheKeyGenerator) {
        this.openAiClient = openAiClient;
        this.intentResolverService = intentResolverService;
        this.kakaoPlaceRecommendationService = kakaoPlaceRecommendationService;
        this.validationService = validationService;
        this.normalizationService = normalizationService;
        this.qualityService = qualityService;
        this.recommendationCacheService = recommendationCacheService;
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    public ChatResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();

        String intent = intentResolverService.resolve(request.getMessage());
        log.info("[CHAT START] intent={}, message={}", intent, request.getMessage());

        if ("RESTAURANT_RECOMMENDATION".equals(intent) || "STAY_RECOMMENDATION".equals(intent)) {
            ChatResponse response = kakaoPlaceRecommendationService.recommend(request);

            long end = System.currentTimeMillis();
            log.info("[CHAT END] intent={}, elapsedMs={}", intent, (end - start));

            return response;
        }

        String cacheKey = cacheKeyGenerator.generate(request.getMessage());

        ChatResponse cached = recommendationCacheService.get(cacheKey);
        if (cached != null) {
            long end = System.currentTimeMillis();
            log.info("[CHAT CACHE HIT] intent={}, cacheKey={}, elapsedMs={}", intent, cacheKey, (end - start));
            return cached;
        }

        long llmStart = System.currentTimeMillis();
        RecommendationDraft draft = openAiClient.generateRecommendationDraft(request.getMessage());
        long llmEnd = System.currentTimeMillis();
        log.info("[CHAT LLM DONE] elapsedMs={}", (llmEnd - llmStart));

        long validationStart = System.currentTimeMillis();
        validationService.validate(draft);

        RecommendationDraft normalized = normalizationService.normalize(draft);
        validationService.validate(normalized);

        RecommendationDraft adjusted = qualityService.adjust(request.getMessage(), normalized);
        validationService.validate(adjusted);
        long validationEnd = System.currentTimeMillis();
        log.info("[CHAT POST PROCESS DONE] elapsedMs={}", (validationEnd - validationStart));

        ChatResponse response = toResponse(request.getMessage(), adjusted);
        recommendationCacheService.put(cacheKey, response);

        long end = System.currentTimeMillis();
        log.info("[CHAT END] intent={}, cacheKey={}, elapsedMs={}", intent, cacheKey, (end - start));

        return response;
    }

    private ChatResponse toResponse(String originalMessage, RecommendationDraft draft) {
        RecommendationContentResponse content = new RecommendationContentResponse(
                toDayPlanResponses(draft.getDayPlans()),
                toItemResponses(draft.getItems())
        );

        return new ChatResponse(
                originalMessage,
                draft.getIntent(),
                draft.getDestination(),
                draft.getDays(),
                content
        );
    }

    private List<DayPlanResponse> toDayPlanResponses(List<DayPlanDraft> dayPlans) {
        List<DayPlanResponse> responses = new ArrayList<>();
        if (dayPlans == null) {
            return responses;
        }

        for (DayPlanDraft dayPlan : dayPlans) {
            responses.add(new DayPlanResponse(
                    dayPlan.getDay(),
                    null,
                    dayPlan.getPlaces()
            ));
        }

        return responses;
    }

    private List<RecommendationItemResponse> toItemResponses(List<RecommendationItemDraft> items) {
        List<RecommendationItemResponse> responses = new ArrayList<>();
        if (items == null) {
            return responses;
        }

        for (RecommendationItemDraft item : items) {
            responses.add(new RecommendationItemResponse(
                    item.getName()
            ));
        }

        return responses;
    }
}