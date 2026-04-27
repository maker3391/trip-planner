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
    private final RegionResolverService regionResolverService;
    private final KakaoPlaceRecommendationService kakaoPlaceRecommendationService;
    private final AttractionRecommendationService attractionRecommendationService;
    private final RecommendationDisplayService recommendationDisplayService;
    private final NaturalExpressionMatcher naturalExpressionMatcher;

    public CombinedRecommendationOrchestratorService(OpenAiClient openAiClient,
                                                     RecommendationValidationService validationService,
                                                     RecommendationNormalizationService normalizationService,
                                                     RecommendationQualityService qualityService,
                                                     RecommendationCacheService recommendationCacheService,
                                                     RecommendationCacheKeyGenerator cacheKeyGenerator,
                                                     ItineraryRequestResolverService itineraryRequestResolverService,
                                                     RegionResolverService regionResolverService,
                                                     KakaoPlaceRecommendationService kakaoPlaceRecommendationService,
                                                     AttractionRecommendationService attractionRecommendationService,
                                                     RecommendationDisplayService recommendationDisplayService,
                                                     NaturalExpressionMatcher naturalExpressionMatcher) {
        this.openAiClient = openAiClient;
        this.validationService = validationService;
        this.normalizationService = normalizationService;
        this.qualityService = qualityService;
        this.recommendationCacheService = recommendationCacheService;
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.itineraryRequestResolverService = itineraryRequestResolverService;
        this.regionResolverService = regionResolverService;
        this.kakaoPlaceRecommendationService = kakaoPlaceRecommendationService;
        this.attractionRecommendationService = attractionRecommendationService;
        this.recommendationDisplayService = recommendationDisplayService;
        this.naturalExpressionMatcher = naturalExpressionMatcher;
    }

    public ChatResponse recommend(ChatRequest request) {
        String message = request.getMessage();
        String value = normalize(message);

        boolean includeItinerary = hasDayExpression(value);
        boolean includeRestaurant = wantsRestaurant(value);
        boolean includeStay = wantsStay(value);
        boolean includeAttraction = wantsAttraction(value);

        ItineraryRequestContext itineraryContext = null;
        String destination;
        String detailArea;
        Integer days = null;

        if (includeItinerary) {
            itineraryContext = itineraryRequestResolverService.resolve(message);
            destination = itineraryContext.getDestination();
            detailArea = itineraryContext.getDetailArea();
            days = itineraryContext.getDays();
        } else {
            RegionResolverService.ResolvedRegion resolvedRegion = regionResolverService.resolve(message);
            destination = resolvedRegion.getCity();
            detailArea = resolvedRegion.getDetailName();

            if (!StringUtils.hasText(destination)) {
                throw new LlmCallException("여행 지역을 해석하지 못했습니다.");
            }
        }

        String locationText = buildLocationText(destination, detailArea);

        String cacheKey = cacheKeyGenerator.generateCombinedKey(destination, detailArea, days)
                + ":" + buildCategoryCacheSuffix(includeItinerary, includeRestaurant, includeStay, includeAttraction);

        ChatResponse cached = recommendationCacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        CompletableFuture<RecommendationContentResponse> itineraryFuture;

        if (includeItinerary) {
            ItineraryRequestContext finalItineraryContext = itineraryContext;
            itineraryFuture = CompletableFuture.supplyAsync(() ->
                    buildItineraryContent(message, finalItineraryContext)
            );
        } else {
            itineraryFuture = CompletableFuture.completedFuture(
                    new RecommendationContentResponse(new ArrayList<>(), new ArrayList<>())
            );
        }

        CompletableFuture<List<RecommendationItemResponse>> restaurantFuture = includeRestaurant
                ? CompletableFuture.supplyAsync(() -> getKakaoItems(locationText + " 맛집 추천"))
                : CompletableFuture.completedFuture(new ArrayList<>());

        CompletableFuture<List<RecommendationItemResponse>> stayFuture = includeStay
                ? CompletableFuture.supplyAsync(() -> getKakaoItems(locationText + " 숙소 추천"))
                : CompletableFuture.completedFuture(new ArrayList<>());

        CompletableFuture<List<RecommendationItemResponse>> attractionFuture = includeAttraction
                ? CompletableFuture.supplyAsync(() -> getAttractionItems(locationText + " 관광지 추천"))
                : CompletableFuture.completedFuture(new ArrayList<>());

        try {
            RecommendationContentResponse itinerary = itineraryFuture.join();
            List<RecommendationItemResponse> restaurants = restaurantFuture.join();
            List<RecommendationItemResponse> stays = stayFuture.join();
            List<RecommendationItemResponse> attractions = attractionFuture.join();

            CombinedRecommendationResponse combined = new CombinedRecommendationResponse(
                    itinerary,
                    restaurants == null ? new ArrayList<>() : restaurants,
                    stays == null ? new ArrayList<>() : stays,
                    attractions == null ? new ArrayList<>() : attractions,
                    includeRestaurant ? recommendationDisplayService.buildCombinedRestaurantTitle(locationText) : null,
                    includeStay ? recommendationDisplayService.buildCombinedStayTitle(locationText) : null,
                    includeAttraction ? buildCombinedAttractionTitle(locationText) : null
            );

            ChatResponse response = new ChatResponse(
                    message,
                    "COMBINED_RECOMMENDATION",
                    destination,
                    days,
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

    private List<RecommendationItemResponse> getKakaoItems(String message) {
        ChatResponse response = kakaoPlaceRecommendationService.recommend(new ChatRequest(message));
        if (response.getRecommendation() == null || response.getRecommendation().getItems() == null) {
            return new ArrayList<>();
        }
        return response.getRecommendation().getItems();
    }

    private List<RecommendationItemResponse> getAttractionItems(String message) {
        ChatResponse response = attractionRecommendationService.recommend(new ChatRequest(message));
        if (response.getRecommendation() == null || response.getRecommendation().getItems() == null) {
            return new ArrayList<>();
        }
        return response.getRecommendation().getItems();
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

    private String buildLocationText(String destination, String detailArea) {
        if (!StringUtils.hasText(destination)) {
            return "";
        }

        if (!StringUtils.hasText(detailArea)) {
            return destination;
        }

        if (destination.equals(detailArea)) {
            return destination;
        }

        return destination + " " + detailArea;
    }

    private String buildCombinedAttractionTitle(String locationText) {
        return locationText + "에서 가볼 만한 명소를 모아봤어요";
    }

    private boolean wantsRestaurant(String value) {
        return containsAny(value,
                "맛집", "음식", "음식점", "식당", "밥집", "먹거리",
                "먹을만한", "먹을 만한", "먹을곳", "먹을 곳",
                "밥", "식사", "한끼", "한 끼",
                "카페", "디저트", "베이커리", "빵집",
                "술집", "주점", "포차", "호프",
                "restaurant", "food", "cafe", "pub", "bar")
                || naturalExpressionMatcher.isRestaurantExpression(value);
    }

    private boolean wantsStay(String value) {
        return containsAny(value,
                "숙소", "숙박", "호텔", "모텔", "펜션", "리조트",
                "게스트하우스", "호스텔", "민박", "풀빌라", "한옥스테이", "에어비앤비",
                "묵을곳", "묵을 곳", "잘곳", "잘 곳", "머물곳", "머물 곳",
                "자고", "잠잘", "잠 잘",
                "하룻밤", "하루 묵", "하루 잘",
                "방 잡", "방잡", "호텔 잡",
                "쉬고 갈", "쉬어갈", "쉬어 갈",
                "stay", "hotel", "accommodation", "motel", "hostel", "resort", "pension")
                || naturalExpressionMatcher.isStayExpression(value);
    }

    private boolean wantsAttraction(String value) {
        return containsAny(value,
                "명소", "관광지", "볼거리", "랜드마크", "핫플", "핫플레이스",
                "가볼만", "가볼 만", "가볼만한", "가볼 만한",
                "가볼곳", "가볼 곳", "가볼만한곳", "가볼 만한 곳",
                "갈만한", "갈 만한", "갈만한곳", "갈 만한 곳",
                "둘러볼", "구경", "놀거리", "놀곳", "놀 곳",
                "데이트코스", "데이트 코스", "드라이브코스", "드라이브 코스", "산책코스", "산책 코스",
                "야경", "산책", "포토존", "인생샷",
                "attraction", "landmark", "sightseeing")
                || naturalExpressionMatcher.isAttractionExpression(value);
    }

    private boolean hasDayExpression(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        return value.matches(".*\\d+\\s*박.*")
                || value.matches(".*\\d+\\s*일.*")
                || value.contains("day")
                || value.contains("days")
                || value.contains("night")
                || value.contains("nights");
    }

    private String buildCategoryCacheSuffix(boolean includeItinerary,
                                            boolean includeRestaurant,
                                            boolean includeStay,
                                            boolean includeAttraction) {
        List<String> parts = new ArrayList<>();

        if (includeItinerary) parts.add("itinerary");
        if (includeRestaurant) parts.add("restaurant");
        if (includeStay) parts.add("stay");
        if (includeAttraction) parts.add("attraction");

        return String.join("-", parts);
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
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