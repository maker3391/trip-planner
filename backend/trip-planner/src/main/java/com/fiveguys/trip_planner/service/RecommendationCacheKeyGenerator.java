package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.ItineraryRequestContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.List;
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
    private final RestaurantKeywordService restaurantKeywordService;

    public RecommendationCacheKeyGenerator(RecommendationIntentResolverService intentResolverService,
                                           RegionResolverService regionResolverService,
                                           RestaurantKeywordService restaurantKeywordService) {
        this.intentResolverService = intentResolverService;
        this.regionResolverService = regionResolverService;
        this.restaurantKeywordService = restaurantKeywordService;
    }

    public String generate(String message) {
        String normalizedMessage = normalize(message);

        String intent = intentResolverService.resolve(message);
        RegionResolverService.ResolvedRegion resolvedRegion = regionResolverService.resolve(message);

        String destination = resolveEffectiveDestination(
                resolvedRegion.getCity(),
                resolvedRegion.getDistrict()
        );

        String district = resolvedRegion.getDistrict();
        String neighborhood = resolvedRegion.getNeighborhood();
        String detailArea = resolvedRegion.getDetailName();

        Integer days = resolveDays(normalizedMessage);
        String subtype = resolveSubtype(intent, normalizedMessage);

        StringBuilder key = new StringBuilder("recommendation:v25");
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

    public String generatePlaceKey(String intent,
                                   String destination,
                                   String detailArea,
                                   String neighborhood,
                                   String district,
                                   String aliasQueryHint,
                                   String rawAreaHint,
                                   String message) {
        String subtype = resolveSubtype(intent, normalize(message));

        StringBuilder key = new StringBuilder("recommendation:v21");
        key.append(":").append(intent);
        key.append(":").append(safeSegment(destination));

        String specificArea = firstNonBlank(
                detailArea,
                neighborhood,
                aliasQueryHint,
                rawAreaHint,
                isCityOrCounty(district) ? null : district
        );

        if (StringUtils.hasText(specificArea)) {
            key.append(":").append(safeSegment(specificArea));
        }

        if (StringUtils.hasText(subtype)) {
            key.append(":").append(subtype);
        }

        return key.toString();
    }

    public String generate(ItineraryRequestContext context) {
        StringBuilder key = new StringBuilder("recommendation:v22");
        key.append(":TRAVEL_ITINERARY");
        key.append(":").append(safeSegment(context.getDestination()));
        key.append(":").append(safeSegment(context.getDetailArea()));
        key.append(":").append(context.getDays() == null ? "unknown" : context.getDays());
        return key.toString();
    }

    public String generateAttractionKey(String destination,
                                        String detailArea,
                                        String neighborhood,
                                        String district,
                                        String message) {
        StringBuilder key = new StringBuilder("recommendation:v25");
        key.append(":ATTRACTION_RECOMMENDATION");
        key.append(":").append(safeSegment(destination));

        String specificArea = firstNonBlank(
                detailArea,
                neighborhood,
                isCityOrCounty(district) ? null : district
        );

        if (StringUtils.hasText(specificArea)) {
            key.append(":").append(safeSegment(specificArea));
        }

        key.append(":").append(resolveAttractionSubtype(message));

        return key.toString();
    }

    private String resolveAttractionSubtype(String message) {
        String normalized = normalize(message);

        if (containsAny(normalized, "랜드마크", "landmark")) {
            return "landmark";
        }

        if (containsAny(normalized, "관광지", "대표 관광지", "sightseeing")) {
            return "sightseeing";
        }

        if (containsAny(normalized, "볼거리")) {
            return "things_to_see";
        }

        if (containsAny(normalized, "가볼만", "attraction")) {
            return "attraction";
        }

        return "spot";
    }

    public String generateCombinedKey(String destination, String detailArea, Integer days) {
        StringBuilder key = new StringBuilder("recommendation:v24");
        key.append(":COMBINED_RECOMMENDATION");
        key.append(":").append(safeSegment(destination));
        key.append(":").append(safeSegment(detailArea));
        key.append(":").append(days == null ? "unknown" : days);
        return key.toString();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String resolveEffectiveDestination(String destination, String district) {
        if (isCityOrCounty(district)) {
            return district;
        }
        return destination;
    }

    private boolean isCityOrCounty(String value) {
        return StringUtils.hasText(value)
                && (value.endsWith("시") || value.endsWith("군"));
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
            if (containsAny(message, "디저트", "베이커리", "빵집", "케이크", "빙수", "와플", "도넛", "아이스크림")) {
                return "dessert";
            }
            if (containsAny(message, "술집", "주점", "포차", "호프", "와인바", "칵테일바", "pub", "bar")) {
                return "pub";
            }

            List<String> foodKeywords = restaurantKeywordService.extractRestaurantFoodKeywords(message);
            if (foodKeywords.isEmpty()) {
                if (containsAny(message, "밥집", "meal")) return "meal";
                return "restaurant";
            }

            String primary = foodKeywords.get(0);

            switch (primary) {
                case "돼지국밥":
                    return "dwaejigukbap";
                case "국밥":
                case "순대국":
                case "설렁탕":
                case "곰탕":
                case "갈비탕":
                case "해장국":
                case "감자탕":
                case "추어탕":
                case "삼계탕":
                case "백숙":
                    return "soup";
                case "김치찌개":
                case "된장찌개":
                case "청국장":
                case "순두부":
                case "부대찌개":
                case "동태찌개":
                case "매운탕":
                case "전골":
                case "곱도리탕":
                case "닭볶음탕":
                case "아구찜":
                case "해물탕":
                case "해물찜":
                case "샤브샤브":
                case "닭한마리":
                    return "stew";
                case "백반":
                case "한정식":
                case "비빔밥":
                case "쌈밥":
                case "보리밥":
                case "기사식당":
                case "꼬막비빔밥":
                    return "meal";
                case "불고기":
                case "삼겹살":
                case "목살":
                case "항정살":
                case "가브리살":
                case "족발":
                case "보쌈":
                case "닭갈비":
                case "제육볶음":
                case "오리구이":
                case "장어구이":
                case "생선구이":
                case "게장":
                case "육회":
                case "육사시미":
                case "닭발":
                case "쭈꾸미":
                case "오징어볶음":
                case "낙곱새":
                case "고기집":
                case "흑돼지":
                    return "meat";
                case "한우":
                case "소고기":
                case "꽃등심":
                case "등심":
                case "안심":
                case "차돌박이":
                case "토시살":
                case "살치살":
                case "안창살":
                case "갈비살":
                case "업진살":
                case "제비추리":
                case "부채살":
                    return "beef";
                case "갈비":
                case "돼지갈비":
                case "소갈비":
                case "양념갈비":
                case "생갈비":
                    return "galbi";
                case "곱창":
                case "대창":
                case "막창":
                case "소막창":
                case "돼지막창":
                    return "gopchang";
                case "냉면":
                case "밀면":
                case "칼국수":
                case "수제비":
                case "국수":
                case "막국수":
                    return "noodle";
                case "초밥":
                case "사시미":
                case "오마카세":
                    return "sushi";
                case "횟집":
                    return "sashimi";
                case "라멘":
                    return "ramen";
                case "우동":
                    return "udon";
                case "소바":
                    return "soba";
                case "돈까스":
                    return "donkatsu";
                case "텐동":
                    return "tendon";
                case "덮밥":
                    return "donburi";
                case "이자카야":
                    return "izakaya";
                case "일식":
                    return "japanese";
                case "짜장면":
                    return "jjajangmyeon";
                case "짬뽕":
                    return "jjamppong";
                case "탕수육":
                    return "tangsuyuk";
                case "마라탕":
                    return "malatang";
                case "마라샹궈":
                    return "malaxiangguo";
                case "훠궈":
                    return "hotpot";
                case "양꼬치":
                    return "lambskewer";
                case "딤섬":
                    return "dimsum";
                case "중식":
                    return "chinese";
                case "파스타":
                    return "pasta";
                case "스테이크":
                    return "steak";
                case "리조또":
                    return "risotto";
                case "피자":
                    return "pizza";
                case "브런치":
                    return "brunch";
                case "샐러드":
                    return "salad";
                case "버거":
                case "수제버거":
                    return "burger";
                case "양식":
                    return "western";
                case "바베큐":
                    return "barbecue";
                case "떡볶이":
                    return "tteokbokki";
                case "김밥":
                    return "gimbap";
                case "순대":
                    return "sundae";
                case "튀김":
                    return "twigim";
                case "분식":
                    return "bunsik";
                case "라볶이":
                    return "rabokki";
                case "오뎅":
                    return "odeng";
                case "토스트":
                    return "toast";
                case "치킨":
                    return "chicken";
                case "닭강정":
                    return "dakgangjeong";
                default:
                    return "restaurant";
            }
        }

        if ("ATTRACTION_RECOMMENDATION".equals(intent)) {
            return "single";
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