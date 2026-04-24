package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;

@Service
public class RecommendationIntentResolverService {

    private final RestaurantKeywordService restaurantKeywordService;

    public RecommendationIntentResolverService(RestaurantKeywordService restaurantKeywordService) {
        this.restaurantKeywordService = restaurantKeywordService;
    }

    public String resolve(String message) {
        String value = normalize(message);

        int restaurantScore = scoreRestaurant(value);
        int stayScore = scoreStay(value);
        int attractionScore = scoreAttraction(value);
        int itineraryScore = scoreItinerary(value);

        int matchedCount = 0;
        if (restaurantScore > 0) matchedCount++;
        if (stayScore > 0) matchedCount++;
        if (attractionScore > 0) matchedCount++;
        if (itineraryScore > 0) matchedCount++;

        if (matchedCount >= 2) {
            return "COMBINED_RECOMMENDATION";
        }

        if (restaurantScore >= stayScore
                && restaurantScore >= attractionScore
                && restaurantScore >= itineraryScore
                && restaurantScore > 0) {
            return "RESTAURANT_RECOMMENDATION";
        }

        if (stayScore >= restaurantScore
                && stayScore >= attractionScore
                && stayScore >= itineraryScore
                && stayScore > 0) {
            return "STAY_RECOMMENDATION";
        }

        if (attractionScore >= restaurantScore
                && attractionScore >= stayScore
                && attractionScore >= itineraryScore
                && attractionScore > 0) {
            return "ATTRACTION_RECOMMENDATION";
        }

        return "TRAVEL_ITINERARY";
    }

    private int scoreRestaurant(String value) {
        int score = 0;

        if (containsAny(value,
                "맛집", "음식", "음식점", "식당", "밥집", "먹거리",
                "먹을만한", "먹을 만한", "먹을곳", "먹을 곳",
                "카페", "디저트", "베이커리", "빵집",
                "술집", "주점", "포차", "호프",
                "restaurant", "food", "cafe", "pub", "bar")) {
            score += 10;
        }

        if (restaurantKeywordService.containsRestaurantFoodKeyword(value)) {
            score += 8;
        }

        return score;
    }

    private int scoreStay(String value) {
        int score = 0;

        if (containsAny(value,
                "숙소", "숙박", "잠잘곳", "잠잘 곳", "잘만한곳", "잘 만한 곳",
                "묵을곳", "묵을 곳", "묵을만한", "묵을 만한",
                "호텔", "리조트", "펜션", "게스트하우스", "모텔", "호스텔",
                "민박", "풀빌라", "한옥스테이", "에어비앤비",
                "stay", "hotel", "accommodation", "motel", "hostel", "resort", "pension")) {
            score += 10;
        }

        return score;
    }

    private int scoreAttraction(String value) {
        int score = 0;

        if (containsAny(value,
                "명소", "관광지", "볼거리", "랜드마크", "대표관광지", "대표 관광지",
                "가볼만", "가볼 만", "가볼만한", "가볼 만한",
                "가볼곳", "가볼 곳", "가볼만한곳", "가볼 만한 곳",
                "둘러볼만한", "둘러볼 만한", "둘러볼곳", "둘러볼 곳",
                "구경할만한", "구경할 만한", "구경할곳", "구경할 곳",
                "놀러갈만한", "놀러갈 만한", "놀거리",
                "핫플", "핫플레이스", "데이트코스", "데이트 코스",
                "attraction", "landmark", "sightseeing", "place to visit", "things to see")) {
            score += 10;
        }

        return score;
    }

    private int scoreItinerary(String value) {
        int score = 0;

        if (containsAny(value,
                "일정", "코스", "여행", "플랜", "동선", "루트",
                "일정짜", "일정 짜", "코스짜", "코스 짜",
                "itinerary", "trip", "travel", "course", "plan", "route")) {
            score += 8;
        }

        if (hasDayExpression(value)) {
            score += 10;
        }

        return score;
    }

    private boolean hasDayExpression(String value) {
        return value.matches(".*\\d+\\s*박.*")
                || value.matches(".*\\d+\\s*일.*")
                || value.contains("day")
                || value.contains("days")
                || value.contains("night")
                || value.contains("nights");
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String message) {
        if (message == null) {
            return "";
        }

        return message
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }
}