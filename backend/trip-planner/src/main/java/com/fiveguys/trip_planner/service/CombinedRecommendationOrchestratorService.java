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
import com.fiveguys.trip_planner.response.CombinedRecommendationResponse;
import com.fiveguys.trip_planner.response.DayPlanResponse;
import com.fiveguys.trip_planner.response.RecommendationContentResponse;
import com.fiveguys.trip_planner.response.RecommendationItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
public class CombinedRecommendationOrchestratorService {

    private final OpenAiClient openAiClient;
    private final RecommendationValidationService validationService;
    private final RecommendationNormalizationService normalizationService;
    private final RecommendationQualityService qualityService;
    private final RecommendationCacheService recommendationCacheService;
    private final RecommendationCacheKeyGenerator cacheKeyGenerator;
    private final ItineraryRequestResolverService itineraryRequestResolverService;
    private final KakaoPlaceRecommendationService kakaoPlaceRecommendationService;
    private final RecommendationDisplayService recommendationDisplayService;

    public CombinedRecommendationOrchestratorService(OpenAiClient openAiClient,
                                                     RecommendationValidationService validationService,
                                                     RecommendationNormalizationService normalizationService,
                                                     RecommendationQualityService qualityService,
                                                     RecommendationCacheService recommendationCacheService,
                                                     RecommendationCacheKeyGenerator cacheKeyGenerator,
                                                     ItineraryRequestResolverService itineraryRequestResolverService,
                                                     KakaoPlaceRecommendationService kakaoPlaceRecommendationService, RecommendationDisplayService recommendationDisplayService) {
        this.openAiClient = openAiClient;
        this.validationService = validationService;
        this.normalizationService = normalizationService;
        this.qualityService = qualityService;
        this.recommendationCacheService = recommendationCacheService;
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.itineraryRequestResolverService = itineraryRequestResolverService;
        this.kakaoPlaceRecommendationService = kakaoPlaceRecommendationService;
        this.recommendationDisplayService = recommendationDisplayService;
    }

    public ChatResponse recommend(ChatRequest request) {
        String message = request.getMessage();
        ItineraryRequestContext context = itineraryRequestResolverService.resolve(message);

        String cacheKey = cacheKeyGenerator.generateCombinedKey(
                context.getDestination(),
                context.getDetailArea(),
                context.getDays()
        );

        ChatResponse cached = recommendationCacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String locationText = buildLocationText(context);

        CompletableFuture<RecommendationContentResponse> itineraryFuture =
                CompletableFuture.supplyAsync(() -> buildItineraryContent(message, context));

        CompletableFuture<List<RecommendationItemResponse>> restaurantFuture =
                CompletableFuture.supplyAsync(() -> {
                    ChatResponse response = kakaoPlaceRecommendationService.recommend(
                            new ChatRequest(locationText + " 맛집 추천")
                    );
                    return response.getRecommendation() == null
                            ? new ArrayList<>()
                            : response.getRecommendation().getItems();
                });

        CompletableFuture<List<RecommendationItemResponse>> stayFuture =
                CompletableFuture.supplyAsync(() -> {
                    ChatResponse response = kakaoPlaceRecommendationService.recommend(
                            new ChatRequest(locationText + " 숙소 추천")
                    );
                    return response.getRecommendation() == null
                            ? new ArrayList<>()
                            : response.getRecommendation().getItems();
                });

        try {
            RecommendationContentResponse itinerary = itineraryFuture.join();
            List<RecommendationItemResponse> restaurants = restaurantFuture.join();
            List<RecommendationItemResponse> stays = stayFuture.join();

            String displayDestination = buildLocationText(context);

            CombinedRecommendationResponse combined = new CombinedRecommendationResponse(
                    itinerary,
                    restaurants == null ? new ArrayList<>() : restaurants,
                    stays == null ? new ArrayList<>() : stays,
                    recommendationDisplayService.buildCombinedRestaurantTitle(displayDestination),
                    recommendationDisplayService.buildCombinedStayTitle(displayDestination)
            );

            ChatResponse response = new ChatResponse(
                    message,
                    "COMBINED_RECOMMENDATION",
                    context.getDestination(),
                    context.getDays(),
                    new RecommendationContentResponse(new ArrayList<>(), new ArrayList<>()),
                    combined
            );

            recommendationCacheService.put(cacheKey, response, Duration.ofHours(2));
            return response;

        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof LlmCallException llmCallException) {
                throw llmCallException;
            }
            throw new LlmCallException("복합 추천 처리 중 오류가 발생했습니다.", cause);
        } catch (Exception e) {
            throw new LlmCallException("복합 추천 처리 중 오류가 발생했습니다.", e);
        }
    }

    private RecommendationContentResponse buildItineraryContent(String message, ItineraryRequestContext context) {
        RecommendationDraft adjusted;

        try {
            adjusted = buildValidatedItineraryDraft(message, context, false);
        } catch (LlmCallException e) {
            if (!shouldRetryItinerary(e)) {
                throw e;
            }
            adjusted = buildValidatedItineraryDraft(message, context, true);
        }

        return new RecommendationContentResponse(
                toDayPlanResponses(adjusted.getDayPlans()),
                toItemResponses(adjusted.getItems())
        );
    }

    private RecommendationDraft buildValidatedItineraryDraft(String message,
                                                             ItineraryRequestContext context,
                                                             boolean expandedScope) {
        ItineraryOnlyDraft itineraryOnlyDraft = openAiClient.generateItineraryDayPlans(context, expandedScope);

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

    private String buildLocationText(ItineraryRequestContext context) {
        if (StringUtils.hasText(context.getDetailArea())) {
            return context.getDestination() + " " + context.getDetailArea();
        }
        return context.getDestination();
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
            responses.add(new RecommendationItemResponse(item.getName()));
        }

        return responses;
    }
}