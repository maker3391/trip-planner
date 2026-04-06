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
    private static final Pattern NIGHTS_ONLY_PATTERN = Pattern.compile("(\\d+)\\s*박");
    private static final Pattern ENGLISH_DAYS_PATTERN = Pattern.compile("(\\d+)\\s*(day|days)");
    private static final Pattern ENGLISH_NIGHTS_DAYS_PATTERN = Pattern.compile("(\\d+)\\s*(night|nights)\\s*(\\d+)\\s*(day|days)");

    private static final Map<String, String> DESTINATION_ALIASES = new LinkedHashMap<>();

    private final DetailAreaParsingService detailAreaParsingService;

    static {
        putAliases("서울",
                "서울", "서울시", "서울특별시",
                "seoul", "seoul city", "seoul special city", "seoul, south korea");

        putAliases("부산",
                "부산", "부산시", "부산광역시",
                "busan", "busan city", "busan metropolitan city", "busan, south korea");

        putAliases("대구",
                "대구", "대구시", "대구광역시",
                "daegu", "daegu city", "daegu metropolitan city");

        putAliases("인천",
                "인천", "인천시", "인천광역시",
                "incheon", "incheon city", "incheon metropolitan city");

        putAliases("광주",
                "광주", "광주시", "광주광역시",
                "gwangju", "gwangju city", "gwangju metropolitan city");

        putAliases("대전",
                "대전", "대전시", "대전광역시",
                "daejeon", "daejeon city", "daejeon metropolitan city");

        putAliases("울산",
                "울산", "울산시", "울산광역시",
                "ulsan", "ulsan city", "ulsan metropolitan city");

        putAliases("세종",
                "세종", "세종시", "세종특별자치시",
                "sejong", "sejong city");

        putAliases("제주",
                "제주", "제주시", "제주도", "제주특별자치도",
                "jeju", "jeju island", "jeju-do");

        putAliases("경기",
                "경기", "경기도",
                "gyeonggi", "gyeonggi-do");

        putAliases("강원",
                "강원", "강원도", "강원특별자치도",
                "gangwon", "gangwon-do");

        putAliases("충북",
                "충북", "충청북도",
                "chungbuk", "chungcheongbuk-do");

        putAliases("충남",
                "충남", "충청남도", "충청도",
                "chungnam", "chungcheongnam-do");

        putAliases("전북",
                "전북", "전라북도", "전북특별자치도",
                "jeonbuk", "jeollabuk-do");

        putAliases("전남",
                "전남", "전라남도", "전라도",
                "jeonnam", "jeollanam-do");

        putAliases("경북",
                "경북", "경상북도",
                "gyeongbuk", "gyeongsangbuk-do");

        putAliases("경남",
                "경남", "경상남도", "경상도",
                "gyeongnam", "gyeongsangnam-do");

        putAliases("여수",
                "여수", "여수시",
                "yeosu", "yeosu-si");

        putAliases("순천",
                "순천", "순천시",
                "suncheon", "suncheon-si");

        putAliases("목포",
                "목포", "목포시",
                "mokpo", "mokpo-si");

        putAliases("전주",
                "전주", "전주시",
                "jeonju", "jeonju-si");

        putAliases("군산",
                "군산", "군산시",
                "gunsan", "gunsan-si");

        putAliases("강릉",
                "강릉", "강릉시",
                "gangneung", "gangneung-si");

        putAliases("속초",
                "속초", "속초시",
                "sokcho", "sokcho-si");

        putAliases("춘천",
                "춘천", "춘천시",
                "chuncheon", "chuncheon-si");

        putAliases("경주",
                "경주", "경주시",
                "gyeongju", "gyeongju-si");

        putAliases("포항",
                "포항", "포항시",
                "pohang", "pohang-si");
    }

    public RecommendationCacheKeyGenerator(DetailAreaParsingService detailAreaParsingService) {
        this.detailAreaParsingService = detailAreaParsingService;
    }

    public String generate(String message) {
        String normalizedMessage = normalize(message);

        String intent = resolveIntent(normalizedMessage);
        String detailArea = detailAreaParsingService.extractDetailArea(message);
        String destination = resolveDestination(normalizedMessage, detailArea);
        Integer days = resolveDays(normalizedMessage);

        StringBuilder key = new StringBuilder("recommendation");
        key.append(":").append(intent);
        key.append(":").append(destination);

        if (StringUtils.hasText(detailArea)) {
            key.append(":").append(normalize(detailArea).replace(" ", "_"));
        }

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

    private String resolveDestination(String message, String detailArea) {
        if (StringUtils.hasText(detailArea)) {
            String parentCity = detailAreaParsingService.resolveParentCity(detailArea);
            if (StringUtils.hasText(parentCity)) {
                return parentCity;
            }
        }

        return DESTINATION_ALIASES.entrySet().stream()
                .filter(entry -> message.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("unknown");
    }

    private Integer resolveDays(String message) {
        Matcher m1 = ENGLISH_NIGHTS_DAYS_PATTERN.matcher(message);
        if (m1.find()) {
            return parseIntSafely(m1.group(3));
        }

        Matcher m2 = NIGHTS_DAYS_PATTERN.matcher(message);
        if (m2.find()) {
            return parseIntSafely(m2.group(2));
        }

        Matcher m3 = ENGLISH_DAYS_PATTERN.matcher(message);
        if (m3.find()) {
            return parseIntSafely(m3.group(1));
        }

        Matcher m4 = DAYS_ONLY_PATTERN.matcher(message);
        if (m4.find()) {
            return parseIntSafely(m4.group(1));
        }

        Matcher m5 = NIGHTS_ONLY_PATTERN.matcher(message);
        if (m5.find()) {
            Integer nights = parseIntSafely(m5.group(1));
            return nights != null ? nights + 1 : null;
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

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase()
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static void putAliases(String canonical, String... aliases) {
        for (String alias : aliases) {
            DESTINATION_ALIASES.put(normalizeStatic(alias), canonical);
        }
    }

    private static String normalizeStatic(String value) {
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