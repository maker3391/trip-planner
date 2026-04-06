package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RecommendationCacheKeyGenerator {

    private static final Pattern NIGHTS_DAYS_PATTERN = Pattern.compile("(\\d+)\\s*박\\s*(\\d+)\\s*일");
    private static final Pattern DAYS_ONLY_PATTERN = Pattern.compile("(\\d+)\\s*일");

    private static final Map<String, String> REGION_ALIASES = new LinkedHashMap<>();

    static {
        REGION_ALIASES.put("서울특별시", "서울");
        REGION_ALIASES.put("서울시", "서울");
        REGION_ALIASES.put("서울", "서울");

        REGION_ALIASES.put("부산광역시", "부산");
        REGION_ALIASES.put("부산시", "부산");
        REGION_ALIASES.put("부산", "부산");

        REGION_ALIASES.put("대구광역시", "대구");
        REGION_ALIASES.put("대구시", "대구");
        REGION_ALIASES.put("대구", "대구");

        REGION_ALIASES.put("인천광역시", "인천");
        REGION_ALIASES.put("인천시", "인천");
        REGION_ALIASES.put("인천", "인천");

        REGION_ALIASES.put("광주광역시", "광주");
        REGION_ALIASES.put("광주시", "광주");
        REGION_ALIASES.put("광주", "광주");

        REGION_ALIASES.put("대전광역시", "대전");
        REGION_ALIASES.put("대전시", "대전");
        REGION_ALIASES.put("대전", "대전");

        REGION_ALIASES.put("울산광역시", "울산");
        REGION_ALIASES.put("울산시", "울산");
        REGION_ALIASES.put("울산", "울산");

        REGION_ALIASES.put("세종특별자치시", "세종");
        REGION_ALIASES.put("세종시", "세종");
        REGION_ALIASES.put("세종", "세종");

        REGION_ALIASES.put("경기도", "경기");
        REGION_ALIASES.put("경기", "경기");

        REGION_ALIASES.put("강원특별자치도", "강원");
        REGION_ALIASES.put("강원도", "강원");
        REGION_ALIASES.put("강원", "강원");

        REGION_ALIASES.put("충청북도", "충북");
        REGION_ALIASES.put("충북", "충북");

        REGION_ALIASES.put("충청남도", "충남");
        REGION_ALIASES.put("충남", "충남");

        REGION_ALIASES.put("전북특별자치도", "전북");
        REGION_ALIASES.put("전라북도", "전북");
        REGION_ALIASES.put("전북", "전북");

        REGION_ALIASES.put("전라남도", "전남");
        REGION_ALIASES.put("전남", "전남");

        REGION_ALIASES.put("경상북도", "경북");
        REGION_ALIASES.put("경북", "경북");

        REGION_ALIASES.put("경상남도", "경남");
        REGION_ALIASES.put("경남", "경남");

        REGION_ALIASES.put("제주특별자치도", "제주");
        REGION_ALIASES.put("제주도", "제주");
        REGION_ALIASES.put("제주", "제주");
    }

    public String generate(String message) {
        String normalizedMessage = normalizeMessage(message);

        String intent = resolveIntent(normalizedMessage);
        String destination = resolveDestination(normalizedMessage);
        Integer days = resolveDays(normalizedMessage);

        StringBuilder key = new StringBuilder("recommendation");
        key.append(":").append(intent);
        key.append(":").append(destination);

        if (days != null) {
            key.append(":").append(days);
        }

        return key.toString();
    }

    private String resolveIntent(String message) {
        if (containsAny(message, "맛집", "음식", "식당", "카페", "먹거리", "restaurant", "food")) {
            return "RESTAURANT_RECOMMENDATION";
        }

        if (containsAny(message, "숙소", "호텔", "리조트", "펜션", "게스트하우스", "stay", "hotel", "accommodation")) {
            return "STAY_RECOMMENDATION";
        }

        return "TRAVEL_ITINERARY";
    }

    private String resolveDestination(String message) {
        for (Map.Entry<String, String> entry : REGION_ALIASES.entrySet()) {
            if (message.contains(normalizeMessage(entry.getKey()))) {
                return entry.getValue();
            }
        }

        return "unknown";
    }

    private Integer resolveDays(String message) {
        Matcher nightsDaysMatcher = NIGHTS_DAYS_PATTERN.matcher(message);
        if (nightsDaysMatcher.find()) {
            return parseIntSafely(nightsDaysMatcher.group(2));
        }

        Matcher daysOnlyMatcher = DAYS_ONLY_PATTERN.matcher(message);
        if (daysOnlyMatcher.find()) {
            return parseIntSafely(daysOnlyMatcher.group(1));
        }

        return null;
    }

    private Integer parseIntSafely(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String normalizeMessage(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase()
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}