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
    private final StaySubtypeResolver staySubtypeResolver;

    public RecommendationCacheKeyGenerator(RecommendationIntentResolverService intentResolverService,
                                           RegionResolverService regionResolverService,
                                           RestaurantKeywordService restaurantKeywordService,
                                           StaySubtypeResolver staySubtypeResolver) {
        this.intentResolverService = intentResolverService;
        this.regionResolverService = regionResolverService;
        this.restaurantKeywordService = restaurantKeywordService;
        this.staySubtypeResolver = staySubtypeResolver;
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

        StringBuilder key = new StringBuilder("recommendation:v26");
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

        StringBuilder key = new StringBuilder("recommendation:v26");
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
        StringBuilder key = new StringBuilder("recommendation:v26");
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
        StringBuilder key = new StringBuilder("recommendation:v26");
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

    public String generateCombinedKey(String destination, String detailArea, Integer days) {
        StringBuilder key = new StringBuilder("recommendation:v26");
        key.append(":COMBINED_RECOMMENDATION");
        key.append(":").append(safeSegment(destination));
        key.append(":").append(safeSegment(detailArea));
        key.append(":").append(days == null ? "unknown" : days);
        return key.toString();
    }

    private String resolveSubtype(String intent, String message) {
        if ("STAY_RECOMMENDATION".equals(intent)) {
            return staySubtypeResolver.resolve(message, intent).value();
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

            return resolveFoodSubtype(foodKeywords.get(0));
        }

        if ("ATTRACTION_RECOMMENDATION".equals(intent)) {
            return resolveAttractionSubtype(message);
        }

        return null;
    }

    private String resolveFoodSubtype(String primary) {
        return switch (primary) {
            case "돼지국밥" -> "dwaejigukbap";
            case "국밥", "순대국", "설렁탕", "곰탕", "갈비탕", "해장국", "감자탕", "추어탕", "삼계탕", "백숙" -> "soup";
            case "김치찌개", "된장찌개", "청국장", "순두부", "부대찌개", "동태찌개", "매운탕", "전골", "곱도리탕", "닭볶음탕", "아구찜", "해물탕", "해물찜", "샤브샤브", "닭한마리" -> "stew";
            case "백반", "한정식", "비빔밥", "쌈밥", "보리밥", "기사식당", "꼬막비빔밥" -> "meal";
            case "불고기", "삼겹살", "목살", "항정살", "가브리살", "족발", "보쌈", "닭갈비", "제육볶음", "오리구이", "장어구이", "생선구이", "게장", "육회", "육사시미", "닭발", "쭈꾸미", "오징어볶음", "낙곱새", "고기집", "흑돼지" -> "meat";
            case "한우", "소고기", "꽃등심", "등심", "안심", "차돌박이", "토시살", "살치살", "안창살", "갈비살", "업진살", "제비추리", "부채살" -> "beef";
            case "갈비", "돼지갈비", "소갈비", "양념갈비", "생갈비" -> "galbi";
            case "곱창", "대창", "막창", "소막창", "돼지막창" -> "gopchang";
            case "냉면", "밀면", "칼국수", "수제비", "국수", "막국수" -> "noodle";
            case "초밥", "사시미", "오마카세" -> "sushi";
            case "횟집" -> "sashimi";
            case "라멘" -> "ramen";
            case "우동" -> "udon";
            case "소바" -> "soba";
            case "돈까스" -> "donkatsu";
            case "텐동" -> "tendon";
            case "덮밥" -> "donburi";
            case "이자카야" -> "izakaya";
            case "일식" -> "japanese";
            case "짜장면" -> "jjajangmyeon";
            case "짬뽕" -> "jjamppong";
            case "탕수육" -> "tangsuyuk";
            case "마라탕" -> "malatang";
            case "마라샹궈" -> "malaxiangguo";
            case "훠궈" -> "hotpot";
            case "양꼬치" -> "lambskewer";
            case "딤섬" -> "dimsum";
            case "중식" -> "chinese";
            case "파스타" -> "pasta";
            case "스테이크" -> "steak";
            case "리조또" -> "risotto";
            case "피자" -> "pizza";
            case "브런치" -> "brunch";
            case "샐러드" -> "salad";
            case "버거", "수제버거" -> "burger";
            case "양식" -> "western";
            case "바베큐" -> "barbecue";
            case "떡볶이" -> "tteokbokki";
            case "김밥" -> "gimbap";
            case "순대" -> "sundae";
            case "튀김" -> "twigim";
            case "분식" -> "bunsik";
            case "라볶이" -> "rabokki";
            case "오뎅" -> "odeng";
            case "토스트" -> "toast";
            case "치킨" -> "chicken";
            case "닭강정" -> "dakgangjeong";
            default -> "restaurant";
        };
    }

    private String resolveAttractionSubtype(String message) {
        String normalized = normalize(message);

        if (containsAny(normalized, "야경", "밤", "나이트뷰")) return "night_view";
        if (containsAny(normalized, "데이트", "데이트코스")) return "date_course";
        if (containsAny(normalized, "산책", "걷기", "둘레길", "올레길")) return "walk";
        if (containsAny(normalized, "자연", "숲", "오름", "해변", "바다", "수목원", "휴양림")) return "nature";
        if (containsAny(normalized, "실내", "비올", "박물관", "미술관", "전시")) return "indoor";
        if (containsAny(normalized, "놀거리", "체험", "액티비티", "테마파크")) return "activity";
        if (containsAny(normalized, "사진", "포토존", "인생샷", "감성")) return "photo_spot";
        if (containsAny(normalized, "드라이브", "해안도로")) return "drive";
        if (containsAny(normalized, "핫플", "핫플레이스", "요즘", "인기")) return "hot_place";
        if (containsAny(normalized, "랜드마크", "landmark")) return "landmark";
        if (containsAny(normalized, "관광지", "대표 관광지", "sightseeing")) return "sightseeing";
        if (containsAny(normalized, "볼거리")) return "things_to_see";
        if (containsAny(normalized, "가볼만", "attraction")) return "attraction";

        return "spot";
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

    private boolean containsAny(String value, String... keywords) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

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