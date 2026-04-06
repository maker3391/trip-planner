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
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final OpenAiClient openAiClient;
    private final RecommendationValidationService validationService;
    private final RecommendationNormalizationService normalizationService;
    private final RecommendationCacheService recommendationCacheService;
    private final RecommendationCacheKeyGenerator cacheKeyGenerator;

    public ChatService(OpenAiClient openAiClient,
                       RecommendationValidationService validationService,
                       RecommendationNormalizationService normalizationService,
                       RecommendationCacheService recommendationCacheService,
                       RecommendationCacheKeyGenerator cacheKeyGenerator) {
        this.openAiClient = openAiClient;
        this.validationService = validationService;
        this.normalizationService = normalizationService;
        this.recommendationCacheService = recommendationCacheService;
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    public ChatResponse chat(ChatRequest request) {
        String cacheKey = cacheKeyGenerator.generate(request.getMessage());

        ChatResponse cached = recommendationCacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        RecommendationDraft draft = openAiClient.generateRecommendationDraft(request.getMessage());

        validationService.validate(draft);

        RecommendationDraft normalized = normalizationService.normalize(draft);

        validationService.validate(normalized);

        ChatResponse response = toResponse(request.getMessage(), normalized);

        recommendationCacheService.put(cacheKey, response);

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
                    item.getName(),
                    null,
                    null
            ));
        }
        return responses;
    }
}