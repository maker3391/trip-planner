package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;

@Service
public class RecommendationIntentResolverService {

    public String resolve(String message) {
        String value = message == null ? "" : message.toLowerCase();

        if (containsAny(value,
                "맛집", "음식", "식당", "카페", "먹거리", "술집", "밥집",
                "restaurant", "food", "cafe")) {
            return "RESTAURANT_RECOMMENDATION";
        }

        if (containsAny(value,
                "숙소", "호텔", "리조트", "펜션", "게스트하우스",
                "모텔", "호스텔", "민박", "풀빌라", "한옥스테이", "에어비앤비",
                "stay", "hotel", "accommodation", "motel", "hostel")) {
            return "STAY_RECOMMENDATION";
        }

        return "TRAVEL_ITINERARY";
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