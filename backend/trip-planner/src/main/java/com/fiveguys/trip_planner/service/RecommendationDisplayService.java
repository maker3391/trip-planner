package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.response.RecommendationItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class RecommendationDisplayService {

    private final RestaurantKeywordService restaurantKeywordService;

    public RecommendationDisplayService(RestaurantKeywordService restaurantKeywordService) {
        this.restaurantKeywordService = restaurantKeywordService;
    }

    public DisplayMeta buildPlaceDisplayMeta(String intent,
                                             String originalMessage,
                                             String destination,
                                             List<RecommendationItemResponse> items) {
        String normalizedDestination = normalize(destination);

        if ("STAY_RECOMMENDATION".equals(intent)) {
            String displayType = resolveStayDisplayType(originalMessage, items);
            String displayTitle = normalizedDestination + "에서 괜찮은 " + attachStayPluralSuffix(displayType) + "을 모아봤어요";
            return new DisplayMeta(displayType, displayTitle);
        }

        String displayType = resolveRestaurantDisplayType(originalMessage, items);
        String displayTitle = normalizedDestination + "에서 가볼 만한 " + attachRestaurantPlaceSuffix(displayType) + "을 모아봤어요";
        return new DisplayMeta(displayType, displayTitle);
    }

    public String buildCombinedRestaurantTitle(String destination) {
        String region = normalize(destination);
        if (!StringUtils.hasText(region)) {
            region = "이 지역";
        }
        return region + "에서 가볼 만한 맛집을 모아봤어요";
    }

    public String buildCombinedStayTitle(String destination) {
        String region = normalize(destination);
        if (!StringUtils.hasText(region)) {
            region = "이 지역";
        }
        return region + "에서 괜찮은 숙소들을 모아봤어요";
    }

    private String resolveRestaurantDisplayType(String originalMessage,
                                                List<RecommendationItemResponse> items) {
        String message = normalize(originalMessage);

        if (restaurantKeywordService.isCafeFocusedRequest(message)) {
            return "카페";
        }

        if (restaurantKeywordService.isPubFocusedRequest(message)) {
            return "술집";
        }

        List<String> keywords = restaurantKeywordService.extractRestaurantFoodKeywords(message);
        if (!keywords.isEmpty()) {
            return keywords.get(0);
        }

        if (containsAny(message, "맛집", "식당", "음식", "밥집", "먹거리", "restaurant", "food")) {
            return "맛집";
        }

        String firstCategory = firstItemCategory(items);
        if (StringUtils.hasText(firstCategory)) {
            return firstCategory;
        }

        return "맛집";
    }

    private String resolveStayDisplayType(String originalMessage,
                                          List<RecommendationItemResponse> items) {
        String message = normalize(originalMessage);

        if (message.contains("풀빌라")) return "풀빌라";
        if (message.contains("한옥스테이")) return "한옥스테이";
        if (message.contains("게스트하우스")) return "게스트하우스";
        if (message.contains("리조트")) return "리조트";
        if (message.contains("펜션")) return "펜션";
        if (message.contains("무인텔") || message.contains("모텔")) return "모텔";
        if (message.contains("호텔")) return "호텔";

        if (containsAny(message, "숙소", "숙박", "stay", "accommodation")) {
            return "숙소";
        }

        String firstCategory = firstItemCategory(items);
        if (StringUtils.hasText(firstCategory)) {
            return firstCategory;
        }

        return "숙소";
    }

    private String firstItemCategory(List<RecommendationItemResponse> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }

        RecommendationItemResponse first = items.get(0);
        if (first == null) {
            return "";
        }

        return normalize(first.getCategory());
    }

    private String attachRestaurantPlaceSuffix(String displayType) {
        if (!StringUtils.hasText(displayType)) {
            return "맛집";
        }

        if ("맛집".equals(displayType) || "카페".equals(displayType) || "술집".equals(displayType)) {
            return displayType;
        }

        return displayType + "집";
    }

    private String attachStayPluralSuffix(String displayType) {
        if (!StringUtils.hasText(displayType)) {
            return "숙소들";
        }

        if ("숙소".equals(displayType)) {
            return "숙소들";
        }

        return displayType + "들";
    }

    private boolean containsAny(String value, String... keywords) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String normalized = value.toLowerCase();
        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    public record DisplayMeta(String displayType, String displayTitle) {
    }
}