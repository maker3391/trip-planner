package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StaySubtypeResolver {

    public StaySubtype resolve(String message, String intent) {
        if (!"STAY_RECOMMENDATION".equals(intent)) {
            return StaySubtype.GENERIC;
        }

        String value = normalize(message);

        if (containsAny(value,
                "오션뷰", "바다뷰", "씨뷰", "seaview", "oceanview",
                "바다보이는", "바다가보이는",
                "해변숙소", "해변호텔", "비치호텔")) {
            return StaySubtype.OCEAN_VIEW;
        }

        if (containsAny(value,
                "감성", "예쁜", "분위기", "인스타", "감각적인",
                "감성숙소", "감성호텔", "예쁜숙소", "예쁜호텔",
                "분위기좋은숙소", "분위기좋은호텔", "인스타감성")) {
            return StaySubtype.EMOTIONAL;
        }

        if (containsAny(value,
                "가성비", "저렴", "싼", "합리적인", "저가",
                "가성비숙소", "가성비호텔",
                "저렴한숙소", "저렴한호텔",
                "싼숙소", "싼호텔",
                "budget", "cheap")) {
            return StaySubtype.BUDGET;
        }

        if (containsAny(value,
                "풀빌라", "프라이빗풀", "수영장숙소", "poolvilla")) {
            return StaySubtype.POOL_VILLA;
        }

        if (containsAny(value,
                "한옥스테이", "한옥숙소", "한옥호텔", "한옥")) {
            return StaySubtype.HANOK;
        }

        if (containsAny(value, "게스트하우스", "게하", "guesthouse", "hostel")) {
            return StaySubtype.GUEST_HOUSE;
        }

        if (containsAny(value, "리조트", "resort")) {
            return StaySubtype.RESORT;
        }

        if (containsAny(value, "펜션", "pension")) {
            return StaySubtype.PENSION;
        }

        if (containsAny(value, "무인텔", "모텔", "motel")) {
            return StaySubtype.MOTEL;
        }

        if (containsAny(value, "호텔", "hotel")) {
            return StaySubtype.HOTEL;
        }

        return StaySubtype.GENERIC;
    }

    private boolean containsAny(String value, String... keywords) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        for (String keyword : keywords) {
            if (value.contains(normalize(keyword))) {
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
                .replaceAll("\\s+", "")
                .trim();
    }
}