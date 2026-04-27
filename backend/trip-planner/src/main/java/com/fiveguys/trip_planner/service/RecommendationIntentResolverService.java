package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;

@Service
public class RecommendationIntentResolverService {

    private final RestaurantKeywordService restaurantKeywordService;
    private final TravelActionResolver travelActionResolver;
    private final NaturalExpressionMatcher naturalExpressionMatcher;

    public RecommendationIntentResolverService(RestaurantKeywordService restaurantKeywordService,
                                               TravelActionResolver travelActionResolver,
                                               NaturalExpressionMatcher naturalExpressionMatcher) {
        this.restaurantKeywordService = restaurantKeywordService;
        this.travelActionResolver = travelActionResolver;
        this.naturalExpressionMatcher = naturalExpressionMatcher;
    }

    public String resolve(String message) {
        String value = normalize(message);

        int restaurantScore = scoreRestaurant(value);
        int stayScore = scoreStay(value);
        int attractionScore = scoreAttraction(value);
        int itineraryScore = scoreItinerary(value);

        int recommendationMatchedCount = 0;
        if (restaurantScore > 0) recommendationMatchedCount++;
        if (stayScore > 0) recommendationMatchedCount++;
        if (attractionScore > 0) recommendationMatchedCount++;

        if (recommendationMatchedCount >= 2) {
            return "COMBINED_RECOMMENDATION";
        }

        if (recommendationMatchedCount >= 1
                && itineraryScore > 0
                && naturalExpressionMatcher.isCombinedExpression(value)) {
            return "COMBINED_RECOMMENDATION";
        }

        TravelActionType actionType = travelActionResolver.resolve(value);

        if (actionType == TravelActionType.EAT && restaurantScore > 0) {
            return "RESTAURANT_RECOMMENDATION";
        }

        if (actionType == TravelActionType.STAY && stayScore > 0) {
            return "STAY_RECOMMENDATION";
        }

        if (actionType == TravelActionType.VISIT && attractionScore > 0) {
            return "ATTRACTION_RECOMMENDATION";
        }

        if (isAttractionStrongKeyword(value)) {
            return "ATTRACTION_RECOMMENDATION";
        }

        if (isRestaurantStrongKeyword(value)) {
            return "RESTAURANT_RECOMMENDATION";
        }

        if (isStayStrongKeyword(value)) {
            return "STAY_RECOMMENDATION";
        }

        if (itineraryScore > 0) {
            return "TRAVEL_ITINERARY";
        }

        if (restaurantScore > 0) {
            return "RESTAURANT_RECOMMENDATION";
        }

        if (stayScore > 0) {
            return "STAY_RECOMMENDATION";
        }

        if (attractionScore > 0) {
            return "ATTRACTION_RECOMMENDATION";
        }

        return "TRAVEL_ITINERARY";
    }

    private int scoreRestaurant(String value) {
        int score = 0;

        if (containsAny(value,
                "맛집", "음식", "음식점", "식당", "밥집", "먹거리",
                "먹을만한", "먹을 만한", "먹을곳", "먹을 곳",
                "밥먹을곳", "밥 먹을 곳", "식사할곳", "식사할 곳",
                "한끼", "한 끼", "끼니",
                "카페", "디저트", "베이커리", "빵집",
                "술집", "주점", "포차", "호프",
                "점심", "저녁", "아침", "야식", "브런치",
                "로컬맛집", "로컬 맛집", "현지인맛집", "현지인 맛집",
                "restaurant", "food", "cafe", "pub", "bar")) {
            score += 10;
        }

        if (naturalExpressionMatcher.isRestaurantExpression(value)) {
            score += 6;
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
                "잘곳", "잘 곳", "머물곳", "머물 곳", "머물만한", "머물 만한",
                "묵을곳", "묵을 곳", "묵을만한", "묵을 만한",
                "호텔", "리조트", "펜션", "게스트하우스", "모텔", "호스텔",
                "민박", "풀빌라", "한옥스테이", "에어비앤비",
                "오션뷰", "바다뷰",
                "감성", "감성숙소", "감성 숙소",
                "가성비", "가성비숙소", "가성비 숙소",
                "stay", "hotel", "accommodation", "motel", "hostel", "resort", "pension")) {
            score += 10;
        }

        if (naturalExpressionMatcher.isStayExpression(value)) {
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
                "갈만한", "갈 만한", "갈만한곳", "갈 만한 곳",
                "둘러볼만한", "둘러볼 만한", "둘러볼곳", "둘러볼 곳",
                "구경할만한", "구경할 만한", "구경할곳", "구경할 곳",
                "놀러갈만한", "놀러갈 만한", "놀거리", "놀곳", "놀 곳",
                "핫플", "핫플레이스", "데이트코스", "데이트 코스",
                "산책", "산책로", "걷기", "걷기 좋은", "걸을만한", "둘레길", "올레길",
                "야경", "밤에", "밤", "나이트뷰",
                "오름", "숲", "자연", "해변", "바다", "수목원", "휴양림", "계곡", "폭포",
                "실내", "비올때", "비 올 때", "박물관", "미술관", "전시", "전시관", "아쿠아리움",
                "사진", "사진 찍기", "사진찍기", "포토존", "인생샷",
                "드라이브", "드라이브코스", "드라이브 코스", "해안도로",
                "체험", "액티비티", "테마파크", "유원지",
                "attraction", "landmark", "sightseeing", "place to visit", "things to see")) {
            score += 10;
        }

        if (naturalExpressionMatcher.isAttractionExpression(value)) {
            score += 10;
        }

        return score;
    }

    private int scoreItinerary(String value) {
        int score = 0;

        if (containsAny(value,
                "데이트코스", "데이트 코스",
                "드라이브코스", "드라이브 코스",
                "산책코스", "산책 코스",
                "야경코스", "야경 코스",
                "사진코스", "사진 코스")) {
            return 0;
        }

        if (containsAny(value,
                "일정", "코스", "여행", "플랜", "동선", "루트",
                "일정짜", "일정 짜", "코스짜", "코스 짜",
                "계획짜", "계획 짜", "여행계획", "여행 계획",
                "itinerary", "trip", "travel", "course", "plan", "route")) {
            score += 8;
        }

        if (naturalExpressionMatcher.isItineraryExpression(value)) {
            score += 8;
        }

        if (hasDayExpression(value)) {
            score += 10;
        }

        return score;
    }

    private boolean isRestaurantStrongKeyword(String value) {
        return containsAny(value,
                "맛집", "밥집", "먹을만한", "먹을 만한",
                "밥먹을곳", "밥 먹을 곳", "식사할곳", "식사할 곳",
                "카페", "디저트", "베이커리", "빵집",
                "술집", "주점", "포차", "호프")
                || naturalExpressionMatcher.isRestaurantStrongExpression(value)
                || restaurantKeywordService.containsRestaurantFoodKeyword(value);
    }

    private boolean isStayStrongKeyword(String value) {
        return containsAny(value,
                "숙소", "숙박", "호텔", "리조트", "펜션", "게스트하우스", "모텔", "호스텔",
                "민박", "풀빌라", "한옥스테이", "에어비앤비",
                "오션뷰", "바다뷰", "감성", "가성비",
                "묵을곳", "묵을 곳", "잘곳", "잘 곳", "머물곳", "머물 곳")
                || naturalExpressionMatcher.isStayStrongExpression(value);
    }

    private boolean isAttractionStrongKeyword(String value) {
        return containsAny(value,
                "데이트코스", "데이트 코스",
                "드라이브코스", "드라이브 코스",
                "산책코스", "산책 코스",
                "산책하기 좋은", "걷기 좋은", "걸을만한",
                "사진 찍기 좋은", "사진찍기 좋은",
                "야경 명소", "야경 추천",
                "실내 명소", "실내 추천",
                "오름 추천", "오름",
                "핫플 추천", "핫플",
                "랜드마크 추천", "랜드마크",
                "포토존", "인생샷",
                "해안도로",
                "테마파크", "유원지",
                "액티비티", "체험",
                "가볼만한곳", "가볼 만한 곳",
                "갈만한곳", "갈 만한 곳",
                "놀거리", "놀 곳", "놀곳")
                || naturalExpressionMatcher.isAttractionStrongExpression(value);
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