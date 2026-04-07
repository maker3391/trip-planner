package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RecommendationCacheKeyGenerator {

    private static final Pattern NIGHTS_DAYS_PATTERN = Pattern.compile("(\\d+)\\s*박\\s*(\\d+)\\s*일");
    private static final Pattern DAYS_ONLY_PATTERN = Pattern.compile("(\\d+)\\s*일");
    private static final Pattern NIGHTS_ONLY_PATTERN = Pattern.compile("(\\d+)\\s*박");
    private static final Pattern ENGLISH_DAYS_PATTERN = Pattern.compile("(\\d+)\\s*(day|days)");
    private static final Pattern ENGLISH_NIGHTS_DAYS_PATTERN = Pattern.compile("(\\d+)\\s*(night|nights)\\s*(\\d+)\\s*(day|days)");

    private final RecommendationIntentResolverService intentResolverService;
    private final RegionResolverService regionResolverService;

    public RecommendationCacheKeyGenerator(RecommendationIntentResolverService intentResolverService,
                                           RegionResolverService regionResolverService) {
        this.intentResolverService = intentResolverService;
        this.regionResolverService = regionResolverService;
    }

    public String generate(String message) {
        String normalizedMessage = normalize(message);

        String intent = intentResolverService.resolve(message);
        RegionResolverService.ResolvedRegion resolvedRegion = regionResolverService.resolve(message);

        String destination = resolvedRegion.getCity();
        String district = resolvedRegion.getDistrict();
        String neighborhood = resolvedRegion.getNeighborhood();
        String detailArea = resolvedRegion.getDetailName();

        if (!StringUtils.hasText(destination) && StringUtils.hasText(district)) {
            destination = district;
        }

        Integer days = resolveDays(normalizedMessage);
        String subtype = resolveSubtype(intent, normalizedMessage);

        StringBuilder key = new StringBuilder("recommendation:v4");
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
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}