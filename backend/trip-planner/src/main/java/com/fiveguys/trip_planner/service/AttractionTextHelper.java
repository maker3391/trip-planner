package com.fiveguys.trip_planner.service;

import org.springframework.util.StringUtils;

public final class AttractionTextHelper {

    private AttractionTextHelper() {
    }

    public static String clean(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim().replaceAll("\\s+", " ");
    }

    public static String normalizeDisplayArea(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim().replaceAll("\\s+", " ");
    }

    public static String normalizeAreaName(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.replaceAll("\\s+", "").trim().toLowerCase();
    }

    public static String normalizeForMatch(String value) {
        return value.toLowerCase()
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static boolean containsKeyword(String value, String... keywords) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String lower = value.toLowerCase();

        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isCityOrCounty(String value) {
        return StringUtils.hasText(value)
                && (value.endsWith("시") || value.endsWith("군"));
    }

    public static String extractCityHead(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        String[] parts = value.trim().split("\\s+");

        if (parts.length == 0) {
            return "";
        }

        String first = parts[0];

        if (first.endsWith("시") || first.endsWith("군")) {
            return first;
        }

        return "";
    }

    public static String resolveEffectiveDestination(String destination, String district) {
        String districtHead = extractCityHead(district);

        if (isCityOrCounty(districtHead)) {
            return districtHead;
        }

        if (isCityOrCounty(district)) {
            return district;
        }

        return destination;
    }

    public static String joinDistinctLocation(String first, String second) {
        if (!StringUtils.hasText(first)) {
            return normalizeDisplayArea(second);
        }

        if (!StringUtils.hasText(second)) {
            return normalizeDisplayArea(first);
        }

        String a = normalizeDisplayArea(first);
        String b = normalizeDisplayArea(second);

        if (normalizeAreaName(a).equals(normalizeAreaName(b))) {
            return a;
        }

        return (a + " " + b).replaceAll("\\s+", " ").trim();
    }

    public static String stripRegionSuffixForLooseMatch(String value) {
        return value.replaceAll("(특별자치도|특별자치시|광역시|특별시|도|시|군|구|동|읍|면|리)$", "")
                .trim();
    }
}