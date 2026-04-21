package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;

@Service
public class RecommendationIntentResolverService {

    private final RestaurantKeywordService restaurantKeywordService;

    public RecommendationIntentResolverService(RestaurantKeywordService restaurantKeywordService) {
        this.restaurantKeywordService = restaurantKeywordService;
    }

    public String resolve(String message) {
        String value = message == null ? "" : message.toLowerCase();

        boolean hasRestaurant = containsAny(value,
                "맛집", "음식", "식당", "카페", "먹거리", "술집", "밥집",
                "restaurant", "food", "cafe")
                || restaurantKeywordService.containsRestaurantFoodKeyword(value);

        boolean hasStay = containsAny(value,
                "숙소", "호텔", "리조트", "펜션", "게스트하우스",
                "모텔", "호스텔", "민박", "풀빌라", "한옥스테이", "에어비앤비",
                "stay", "hotel", "accommodation", "motel", "hostel");

        boolean hasItinerary = containsAny(value,
                "일정", "코스", "여행", "플랜", "동선",
                "itinerary", "trip", "travel", "course", "plan")
                || hasDayExpression(value);

        boolean hasAttraction = containsAny(value,
                "명소", "관광지", "가볼만", "볼거리", "랜드마크", "대표 관광지",
                "attraction", "landmark", "sightseeing");

        if (hasRestaurant && hasStay && hasItinerary) {
            return "COMBINED_RECOMMENDATION";
        }

        if (hasAttraction && !hasRestaurant && !hasStay) {
            return "ATTRACTION_RECOMMENDATION";
        }

        if (hasRestaurant) {
            return "RESTAURANT_RECOMMENDATION";
        }

        if (hasStay) {
            return "STAY_RECOMMENDATION";
        }

        return "TRAVEL_ITINERARY";
    }

    private boolean hasDayExpression(String value) {
        return value.contains("박") || value.contains("일") || value.contains("day") || value.contains("days");
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}