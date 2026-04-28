package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.ItineraryRequestContext;
import com.fiveguys.trip_planner.exception.LlmCallException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ItineraryRequestResolverService {

    private static final Pattern NIGHTS_DAYS_PATTERN = Pattern.compile("(\\d+)\\s*박\\s*(\\d+)\\s*일");
    private static final Pattern DAYS_ONLY_PATTERN = Pattern.compile("(\\d+)\\s*일");
    private static final Pattern NIGHTS_ONLY_PATTERN = Pattern.compile("(\\d+)\\s*박");
    private static final Pattern ENGLISH_DAYS_PATTERN = Pattern.compile("(\\d+)\\s*(day|days)");
    private static final Pattern ENGLISH_NIGHTS_DAYS_PATTERN = Pattern.compile("(\\d+)\\s*(night|nights)\\s*(\\d+)\\s*(day|days)");

    private final RegionResolverService regionResolverService;
    private final DetailAreaParsingService detailAreaParsingService;
    private final RegionAliasResolverService regionAliasResolverService;

    public ItineraryRequestResolverService(RegionResolverService regionResolverService,
                                           DetailAreaParsingService detailAreaParsingService,
                                           RegionAliasResolverService regionAliasResolverService) {
        this.regionResolverService = regionResolverService;
        this.detailAreaParsingService = detailAreaParsingService;
        this.regionAliasResolverService = regionAliasResolverService;
    }

    public ItineraryRequestContext resolve(String message) {
        RegionResolverService.ResolvedRegion resolvedRegion = regionResolverService.resolve(message);

        RegionAliasResolverService.ResolvedAlias alias =
                regionAliasResolverService.resolve(message, resolvedRegion.getCity());

        String aliasTargetName = alias != null ? alias.getTargetName() : null;
        String aliasQueryHint = alias != null ? alias.getQueryHint() : null;
        String aliasCity = alias != null ? alias.getCity() : null;

        String broadOrProvince = resolveBroadOrProvince(message);

        String detailArea = resolveDetailArea(
                resolvedRegion.getDetailName(),
                resolvedRegion.getNeighborhood(),
                aliasTargetName,
                aliasQueryHint,
                message
        );

        String destination = resolveDestination(
                resolvedRegion.getCity(),
                resolvedRegion.getDistrict(),
                detailArea,
                aliasCity,
                broadOrProvince
        );

        Integer days = resolveDays(message);

        if (!StringUtils.hasText(destination)) {
            throw new LlmCallException("여행 지역을 해석하지 못했습니다.");
        }

        if (days == null || days < 1) {
            throw new LlmCallException("여행 일수를 해석하지 못했습니다. 예) 부산 2박 3일 여행 일정 추천, 강릉 3박4일 일정 짜줘");
        }

        return new ItineraryRequestContext(destination, detailArea, days);
    }

    private String resolveDetailArea(String detailName,
                                     String neighborhood,
                                     String aliasTargetName,
                                     String aliasQueryHint,
                                     String originalMessage) {
        String extracted = detailAreaParsingService.extractDetailArea(detailName);
        if (StringUtils.hasText(extracted)) {
            return extracted;
        }

        extracted = detailAreaParsingService.extractDetailArea(neighborhood);
        if (StringUtils.hasText(extracted)) {
            return extracted;
        }

        extracted = detailAreaParsingService.extractDetailArea(aliasTargetName);
        if (StringUtils.hasText(extracted)) {
            return extracted;
        }

        extracted = detailAreaParsingService.extractDetailArea(aliasQueryHint);
        if (StringUtils.hasText(extracted)) {
            return extracted;
        }

        extracted = detailAreaParsingService.extractDetailArea(originalMessage);
        if (StringUtils.hasText(extracted)) {
            return extracted;
        }

        return null;
    }

    private String resolveDestination(String city,
                                      String district,
                                      String detailArea,
                                      String aliasCity,
                                      String broadOrProvince) {
        if (StringUtils.hasText(detailArea)) {
            String parentCity = detailAreaParsingService.resolveParentCity(detailArea);
            if (StringUtils.hasText(parentCity)) {
                return canonicalizeProvinceOrBroad(parentCity);
            }
        }

        if (StringUtils.hasText(aliasCity)) {
            return canonicalizeProvinceOrBroad(aliasCity);
        }

        if (isCityOrCounty(district)) {
            return canonicalizeProvinceOrBroad(district);
        }

        if (StringUtils.hasText(city)) {
            return canonicalizeProvinceOrBroad(city);
        }

        if (StringUtils.hasText(district)) {
            return canonicalizeProvinceOrBroad(district);
        }

        return canonicalizeProvinceOrBroad(broadOrProvince);
    }

    private String resolveBroadOrProvince(String message) {
        if (!StringUtils.hasText(message)) {
            return null;
        }

        String value = message.replaceAll("\\s+", "");

        if (value.contains("경상북도") || value.contains("경상북") || value.contains("경북")) return "경상북도";
        if (value.contains("경상남도") || value.contains("경상남") || value.contains("경남")) return "경상남도";
        if (value.contains("전라북도") || value.contains("전라북") || value.contains("전북")) return "전라북도";
        if (value.contains("전라남도") || value.contains("전라남") || value.contains("전남")) return "전라남도";
        if (value.contains("충청북도") || value.contains("충청북") || value.contains("충북")) return "충청북도";
        if (value.contains("충청남도") || value.contains("충청남") || value.contains("충남")) return "충청남도";
        if (value.contains("경기도") || value.contains("경기")) return "경기도";
        if (value.contains("강원특별자치도") || value.contains("강원도") || value.contains("강원")) return "강원도";
        if (value.contains("제주특별자치도") || value.contains("제주도")) return "제주도";

        if (value.contains("경상도")) return "경상도";
        if (value.contains("전라도")) return "전라도";
        if (value.contains("충청도")) return "충청도";

        return null;
    }

    private String canonicalizeProvinceOrBroad(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String compact = value.replaceAll("\\s+", "");

        if (compact.equals("경북") || compact.equals("경상북") || compact.equals("경상북도")) return "경상북도";
        if (compact.equals("경남") || compact.equals("경상남") || compact.equals("경상남도")) return "경상남도";
        if (compact.equals("전북") || compact.equals("전라북") || compact.equals("전라북도")) return "전라북도";
        if (compact.equals("전남") || compact.equals("전라남") || compact.equals("전라남도")) return "전라남도";
        if (compact.equals("충북") || compact.equals("충청북") || compact.equals("충청북도")) return "충청북도";
        if (compact.equals("충남") || compact.equals("충청남") || compact.equals("충청남도")) return "충청남도";
        if (compact.equals("경기") || compact.equals("경기도")) return "경기도";
        if (compact.equals("강원") || compact.equals("강원도") || compact.equals("강원특별자치도")) return "강원도";
        if (compact.equals("제주도") || compact.equals("제주특별자치도")) return "제주도";

        if (compact.equals("경상도")) return "경상도";
        if (compact.equals("전라도")) return "전라도";
        if (compact.equals("충청도")) return "충청도";

        return value;
    }

    private boolean isCityOrCounty(String value) {
        return StringUtils.hasText(value)
                && (value.endsWith("시") || value.endsWith("군"));
    }

    private Integer resolveDays(String message) {
        if (!StringUtils.hasText(message)) {
            return null;
        }

        if (message.contains("일주일") || message.contains("한 주")) {
            return 7;
        }

        Matcher weekMatcher = Pattern.compile("(\\d+)\\s*주").matcher(message);
        if (weekMatcher.find()) {
            Integer weeks = parseIntSafely(weekMatcher.group(1));
            if (weeks != null) return weeks * 7;
        }

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
}