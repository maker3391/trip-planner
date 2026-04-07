package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.ParsedRegion;
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
    private final RegionParsingService regionParsingService;

    static {
        registerDestination("서울", "서울", "서울시", "서울특별시", "seoul");
        registerDestination("부산", "부산", "부산시", "부산광역시", "busan");
        registerDestination("대구", "대구", "대구시", "대구광역시", "daegu");
        registerDestination("인천", "인천", "인천시", "인천광역시", "incheon");
        registerDestination("광주", "광주", "광주시", "광주광역시", "gwangju");
        registerDestination("대전", "대전", "대전시", "대전광역시", "daejeon");
        registerDestination("울산", "울산", "울산시", "울산광역시", "ulsan");
        registerDestination("세종", "세종", "세종시", "세종특별자치시", "sejong");
        registerDestination("제주", "제주", "제주도", "제주특별자치도", "jeju");
        registerDestination("경기", "경기", "경기도", "gyeonggi");
        registerDestination("강원", "강원", "강원도", "강원특별자치도", "gangwon");
        registerDestination("충북", "충북", "충청북도", "chungbuk");
        registerDestination("충남", "충남", "충청남도", "chungnam");
        registerDestination("전북", "전북", "전라북도", "전북특별자치도", "jeonbuk");
        registerDestination("전남", "전남", "전라남도", "jeonnam");
        registerDestination("경북", "경북", "경상북도", "gyeongbuk");
        registerDestination("경남", "경남", "경상남도", "gyeongnam");
    }

    public RecommendationCacheKeyGenerator(DetailAreaParsingService detailAreaParsingService,
                                           RegionParsingService regionParsingService) {
        this.detailAreaParsingService = detailAreaParsingService;
        this.regionParsingService = regionParsingService;
    }

    public String generate(String message) {
        String normalizedMessage = normalize(message);

        String intent = resolveIntent(normalizedMessage);
        String detailArea = detailAreaParsingService.extractDetailArea(message);
        ParsedRegion parsedRegion = regionParsingService.parse(message, "");

        String destination = resolveDestination(normalizedMessage, detailArea, parsedRegion);
        String district = parsedRegion == null ? null : parsedRegion.getDistrict();
        String neighborhood = parsedRegion == null ? null : parsedRegion.getNeighborhood();
        Integer days = resolveDays(normalizedMessage);
        String subtype = resolveSubtype(intent, normalizedMessage);

        StringBuilder key = new StringBuilder("recommendation:v3");
        key.append(":").append(intent);
        key.append(":").append(safeSegment(destination));

        if (StringUtils.hasText(detailArea)) {
            key.append(":").append(safeSegment(detailArea));
        } else if (StringUtils.hasText(neighborhood)) {
            key.append(":").append(safeSegment(neighborhood));
        } else if (StringUtils.hasText(district)) {
            key.append(":").append(safeSegment(district));
        }

        if (StringUtils.hasText(subtype)) {
            key.append(":").append(subtype);
        }

        if (days != null) {
            key.append(":").append(days);
        }

        return key.toString();
    }

    private String resolveIntent(String message) {
        if (containsAny(message,
                "맛집", "음식", "식당", "카페", "먹거리", "술집", "밥집",
                "restaurant", "food", "cafe")) {
            return "RESTAURANT_RECOMMENDATION";
        }

        if (containsAny(message,
                "숙소", "호텔", "리조트", "펜션", "게스트하우스",
                "모텔", "호스텔", "민박", "풀빌라", "한옥스테이", "에어비앤비",
                "stay", "hotel", "accommodation", "motel", "hostel")) {
            return "STAY_RECOMMENDATION";
        }

        return "TRAVEL_ITINERARY";
    }

    private String resolveSubtype(String intent, String message) {
        if ("STAY_RECOMMENDATION".equals(intent)) {
            if (containsAny(message, "모텔", "motel")) return "motel";
            if (containsAny(message, "펜션", "pension")) return "pension";
            if (containsAny(message, "게스트하우스", "guesthouse")) return "guesthouse";
            if (containsAny(message, "리조트", "resort")) return "resort";
            if (containsAny(message, "호스텔", "hostel")) return "hostel";
            if (containsAny(message, "민박", "minbak")) return "minbak";
            if (containsAny(message, "풀빌라", "poolvilla")) return "poolvilla";
            if (containsAny(message, "한옥스테이", "hanokstay")) return "hanokstay";
            if (containsAny(message, "에어비앤비", "airbnb")) return "airbnb";
            if (containsAny(message, "호텔", "hotel")) return "hotel";
            return "stay";
        }

        if ("RESTAURANT_RECOMMENDATION".equals(intent)) {
            if (containsAny(message, "카페", "cafe")) return "cafe";
            if (containsAny(message, "술집", "pub", "bar")) return "pub";
            if (containsAny(message, "밥집", "meal")) return "meal";
            return "restaurant";
        }

        return null;
    }

    private String resolveDestination(String normalizedMessage, String detailArea, ParsedRegion parsedRegion) {
        if (StringUtils.hasText(detailArea)) {
            String parentCity = detailAreaParsingService.resolveParentCity(detailArea);
            if (StringUtils.hasText(parentCity)) {
                return parentCity;
            }
        }

        if (parsedRegion != null) {
            if (StringUtils.hasText(parsedRegion.getCity())) {
                return parsedRegion.getCity();
            }
            if (StringUtils.hasText(parsedRegion.getProvince())) {
                return parsedRegion.getProvince();
            }
        }

        for (Map.Entry<String, String> entry : DESTINATION_ALIASES.entrySet()) {
            if (normalizedMessage.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "unknown";
    }

    private Integer resolveDays(String message) {
        Matcher m1 = ENGLISH_NIGHTS_DAYS_PATTERN.matcher(message);
        if (m1.find()) return parseIntSafely(m1.group(3));

        Matcher m2 = NIGHTS_DAYS_PATTERN.matcher(message);
        if (m2.find()) return parseIntSafely(m2.group(2));

        Matcher m3 = ENGLISH_DAYS_PATTERN.matcher(message);
        if (m3.find()) return parseIntSafely(m3.group(1));

        Matcher m4 = DAYS_ONLY_PATTERN.matcher(message);
        if (m4.find()) return parseIntSafely(m4.group(1));

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

    private String safeSegment(String value) {
        String normalized = normalize(value);
        return StringUtils.hasText(normalized)
                ? normalized.replace(" ", "_")
                : "unknown";
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

    private static void registerDestination(String canonical, String... aliases) {
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