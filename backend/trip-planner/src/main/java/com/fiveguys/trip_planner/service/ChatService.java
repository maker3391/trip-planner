package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.client.OpenAiClient;
import com.fiveguys.trip_planner.dto.ChatRequest;
import com.fiveguys.trip_planner.dto.DayPlanDraft;
import com.fiveguys.trip_planner.dto.ItineraryOnlyDraft;
import com.fiveguys.trip_planner.dto.ItineraryRequestContext;
import com.fiveguys.trip_planner.dto.RecommendationDraft;
import com.fiveguys.trip_planner.dto.RecommendationItemDraft;
import com.fiveguys.trip_planner.exception.LlmCallException;
import com.fiveguys.trip_planner.response.ChatResponse;
import com.fiveguys.trip_planner.response.DayPlanResponse;
import com.fiveguys.trip_planner.response.RecommendationContentResponse;
import com.fiveguys.trip_planner.response.RecommendationItemResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final OpenAiClient openAiClient;
    private final RecommendationIntentResolverService intentResolverService;
    private final KakaoPlaceRecommendationService kakaoPlaceRecommendationService;
    private final AttractionRecommendationService attractionRecommendationService;
    private final CombinedRecommendationOrchestratorService combinedRecommendationOrchestratorService;
    private final RecommendationValidationService validationService;
    private final RecommendationNormalizationService normalizationService;
    private final RecommendationQualityService qualityService;
    private final RecommendationCacheService recommendationCacheService;
    private final RecommendationCacheKeyGenerator cacheKeyGenerator;
    private final ItineraryRequestResolverService itineraryRequestResolverService;

    public ChatService(OpenAiClient openAiClient,
                       RecommendationIntentResolverService intentResolverService,
                       KakaoPlaceRecommendationService kakaoPlaceRecommendationService,
                       AttractionRecommendationService attractionRecommendationService,
                       CombinedRecommendationOrchestratorService combinedRecommendationOrchestratorService,
                       RecommendationValidationService validationService,
                       RecommendationNormalizationService normalizationService,
                       RecommendationQualityService qualityService,
                       RecommendationCacheService recommendationCacheService,
                       RecommendationCacheKeyGenerator cacheKeyGenerator,
                       ItineraryRequestResolverService itineraryRequestResolverService) {
        this.openAiClient = openAiClient;
        this.intentResolverService = intentResolverService;
        this.kakaoPlaceRecommendationService = kakaoPlaceRecommendationService;
        this.attractionRecommendationService = attractionRecommendationService;
        this.combinedRecommendationOrchestratorService = combinedRecommendationOrchestratorService;
        this.validationService = validationService;
        this.normalizationService = normalizationService;
        this.qualityService = qualityService;
        this.recommendationCacheService = recommendationCacheService;
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.itineraryRequestResolverService = itineraryRequestResolverService;
    }

    public ChatResponse chat(ChatRequest request) {
        String message = request.getMessage();

        if (isMeaninglessInput(message)) {
            throw new LlmCallException("요청을 이해하지 못했습니다");
        }

        String intent = intentResolverService.resolve(message);

        if ("COMBINED_RECOMMENDATION".equals(intent)) {
            return combinedRecommendationOrchestratorService.recommend(request);
        }

        if ("ATTRACTION_RECOMMENDATION".equals(intent)) {
            return attractionRecommendationService.recommend(request);
        }

        if ("RESTAURANT_RECOMMENDATION".equals(intent) || "STAY_RECOMMENDATION".equals(intent)) {
            return kakaoPlaceRecommendationService.recommend(request);
        }

        ItineraryRequestContext context = itineraryRequestResolverService.resolve(message);

        if (context.getDays() <= 0 || context.getDays() > 7) {
            throw new IllegalArgumentException("여행 일정은 최대 7일까지 추천 가능합니다");
        }

        String cacheKey = cacheKeyGenerator.generate(context);

        ChatResponse cached = recommendationCacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        RecommendationDraft adjusted;

        try {
            adjusted = buildValidatedItineraryDraft(message, context);
        } catch (LlmCallException e) {
            if (!shouldRetryItinerary(e)) {
                throw e;
            }
            adjusted = buildValidatedItineraryDraft(message, context);
        }

        ChatResponse response = toResponse(message, adjusted);
        recommendationCacheService.put(cacheKey, response);

        return response;
    }

    private boolean isMeaninglessInput(String message) {
        if (message == null || message.isBlank()) {
            return true;
        }

        String trimmed = message.trim();

        if (trimmed.length() <= 2) {
            return true;
        }

        if (!trimmed.matches(".*[가-힣a-zA-Z0-9].*")) {
            return true;
        }

        return false;
    }

    private RecommendationDraft buildValidatedItineraryDraft(String message,
                                                             ItineraryRequestContext context) {
        ItineraryOnlyDraft itineraryOnlyDraft = openAiClient.generateItineraryDayPlans(context);

        RecommendationDraft rawDraft = new RecommendationDraft();
        rawDraft.setIntent("TRAVEL_ITINERARY");
        rawDraft.setDestination(context.getDestination());
        rawDraft.setDetailArea(context.getDetailArea());
        rawDraft.setDays(context.getDays());
        rawDraft.setDayPlans(itineraryOnlyDraft.getDayPlans() == null ? new ArrayList<>() : itineraryOnlyDraft.getDayPlans());
        rawDraft.setItems(new ArrayList<>());

        RecommendationDraft normalized = normalizationService.normalize(rawDraft);
        RecommendationDraft adjusted = qualityService.adjust(message, normalized);
        validationService.validate(adjusted);

        return adjusted;
    }

    private boolean shouldRetryItinerary(LlmCallException e) {
        if (e == null || e.getMessage() == null) {
            return false;
        }

        String message = e.getMessage();
        return message.contains("여행 일정 추천 결과가 비어 있습니다.")
                || message.contains("여행 일수와 일정 개수가 맞지 않습니다.")
                || message.contains("추천 장소가 충분하지 않습니다.");
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