package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TravelActionResolver {

    public TravelActionType resolve(String message) {
        String value = normalize(message);

        if (!StringUtils.hasText(value)) {
            return TravelActionType.UNKNOWN;
        }

        if (containsAny(value,
                "먹", "밥", "식사", "한끼", "한 끼", "맛집", "음식", "음식점", "식당",
                "카페", "디저트", "베이커리", "빵집",
                "술집", "주점", "포차", "호프")) {
            return TravelActionType.EAT;
        }

        if (containsAny(value,
                "숙소", "숙박", "호텔", "모텔", "펜션", "리조트", "게스트하우스",
                "호스텔", "민박", "풀빌라", "한옥스테이", "에어비앤비",
                "묵", "잠", "잘 곳", "잘곳", "머물")) {
            return TravelActionType.STAY;
        }

        if (containsAny(value,
                "명소", "관광지", "볼거리", "랜드마크", "핫플", "핫플레이스",
                "가볼", "갈만", "갈 만", "둘러볼", "구경", "놀거리", "놀 곳", "놀곳",
                "데이트코스", "드라이브코스", "산책코스",
                "야경", "산책", "걷기", "오름", "숲", "자연", "해변", "바다",
                "박물관", "미술관", "전시", "아쿠아리움",
                "포토존", "인생샷", "테마파크", "유원지", "액티비티")) {
            return TravelActionType.VISIT;
        }

        if (containsAny(value,
                "일정", "여행", "플랜", "동선", "루트", "일정짜", "일정 짜",
                "코스짜", "코스 짜")) {
            return TravelActionType.PLAN;
        }

        if (isPlainCourseRequest(value)) {
            return TravelActionType.PLAN;
        }

        return TravelActionType.UNKNOWN;
    }

    private boolean isPlainCourseRequest(String value) {
        return value.contains("코스")
                && !containsAny(value,
                "데이트코스", "데이트 코스",
                "드라이브코스", "드라이브 코스",
                "산책코스", "산책 코스",
                "야경코스", "야경 코스",
                "사진코스", "사진 코스");
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
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
}