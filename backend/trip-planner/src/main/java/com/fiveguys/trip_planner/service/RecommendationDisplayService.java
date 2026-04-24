package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.response.RecommendationItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class RecommendationDisplayService {

    private final RestaurantKeywordService restaurantKeywordService;
    private final StaySubtypeResolver staySubtypeResolver;

    public RecommendationDisplayService(RestaurantKeywordService restaurantKeywordService,
                                        StaySubtypeResolver staySubtypeResolver) {
        this.restaurantKeywordService = restaurantKeywordService;
        this.staySubtypeResolver = staySubtypeResolver;
    }

    public DisplayMeta buildPlaceDisplayMeta(String intent,
                                             String originalMessage,
                                             String destination,
                                             List<RecommendationItemResponse> items) {
        String normalizedDestination = normalizeDisplayDestination(destination);

        if ("STAY_RECOMMENDATION".equals(intent)) {
            String displayType = resolveStayDisplayType(originalMessage, intent);
            String displayTitle = normalizedDestination + "에서 괜찮은 " + attachStayPluralSuffix(displayType) + "을 모아봤어요";
            return new DisplayMeta(displayType, displayTitle);
        }

        String displayType = resolveRestaurantDisplayType(originalMessage);
        String displayTitle = normalizedDestination + "에서 가볼 만한 " + attachRestaurantPlaceSuffix(displayType) + "을 모아봤어요";
        return new DisplayMeta(displayType, displayTitle);
    }

    public String buildCombinedRestaurantTitle(String destination) {
        String region = normalizeDisplayDestination(destination);
        return region + "에서 가볼 만한 맛집을 모아봤어요";
    }

    public String buildCombinedStayTitle(String destination) {
        String region = normalizeDisplayDestination(destination);
        return region + "에서 괜찮은 숙소들을 모아봤어요";
    }

    private String resolveRestaurantDisplayType(String originalMessage) {
        String message = normalize(originalMessage);

        if (restaurantKeywordService.isCafeFocusedRequest(message)) {
            return "카페";
        }

        if (restaurantKeywordService.isPubFocusedRequest(message)) {
            return "술집";
        }

        List<String> keywords = restaurantKeywordService.extractRestaurantFoodKeywords(message);

        if (!keywords.isEmpty()) {
            return keywords.get(0);
        }

        return "맛집";
    }

    private String resolveStayDisplayType(String originalMessage, String intent) {
        StaySubtype subtype = staySubtypeResolver.resolve(originalMessage, intent);

        return switch (subtype) {
            case POOL_VILLA -> "풀빌라";
            case HANOK -> "한옥스테이";
            case GUEST_HOUSE -> "게스트하우스";
            case RESORT -> "리조트";
            case PENSION -> "펜션";
            case MOTEL -> "모텔";
            case HOTEL -> "호텔";
            default -> "숙소";
        };
    }

    private String attachRestaurantPlaceSuffix(String displayType) {
        if (!StringUtils.hasText(displayType)) {
            return "맛집";
        }

        if ("맛집".equals(displayType) || "카페".equals(displayType) || "술집".equals(displayType)) {
            return displayType;
        }

        return displayType + "집";
    }

    private String attachStayPluralSuffix(String displayType) {
        if (!StringUtils.hasText(displayType)) {
            return "숙소들";
        }

        if ("숙소".equals(displayType)) {
            return "숙소들";
        }

        return displayType + "들";
    }

    public DisplayMeta buildAttractionDisplayMeta(String originalMessage, String destination) {
        String message = normalize(originalMessage);
        String region = normalizeDisplayDestination(destination);

        if (containsAny(message, "사진 찍기", "사진찍기", "사진", "포토존", "인생샷")) {
            return new DisplayMeta("포토스팟", region + "에서 사진 찍기 좋은 곳을 모아봤어요");
        }

        if (containsAny(message, "핫플", "핫플레이스", "요즘", "인기", "트렌디")) {
            return new DisplayMeta("핫플", region + "에서 가볼 만한 핫플을 모아봤어요");
        }

        if (containsAny(message, "야경", "밤에", "나이트뷰")) {
            return new DisplayMeta("야경 명소", region + "에서 야경 보기 좋은 곳을 모아봤어요");
        }

        if (containsAny(message, "데이트코스", "데이트 코스", "데이트")) {
            return new DisplayMeta("데이트코스", region + "에서 데이트하기 좋은 곳을 모아봤어요");
        }

        if (containsAny(message, "산책", "산책로", "걷기", "걷기 좋은", "둘레길", "올레길")) {
            return new DisplayMeta("산책 명소", region + "에서 산책하기 좋은 곳을 모아봤어요");
        }

        if (containsAny(message, "실내", "비올때", "비 올 때", "박물관", "미술관", "전시", "전시관")) {
            return new DisplayMeta("실내 명소", region + "에서 실내로 가기 좋은 곳을 모아봤어요");
        }

        if (containsAny(message, "자연", "오름", "숲", "해변", "바다", "수목원", "휴양림", "계곡", "폭포")) {
            return new DisplayMeta("자연 명소", region + "에서 자연을 느끼기 좋은 곳을 모아봤어요");
        }

        if (containsAny(message, "드라이브", "드라이브코스", "해안도로")) {
            return new DisplayMeta("드라이브코스", region + "에서 드라이브하기 좋은 곳을 모아봤어요");
        }

        if (containsAny(message, "놀거리", "체험", "액티비티", "테마파크", "유원지")) {
            return new DisplayMeta("놀거리", region + "에서 즐길 만한 놀거리를 모아봤어요");
        }

        if (containsAny(message, "랜드마크", "landmark")) {
            return new DisplayMeta("랜드마크", region + "에서 대표 랜드마크를 모아봤어요");
        }

        if (containsAny(message, "관광지", "대표 관광지", "sightseeing")) {
            return new DisplayMeta("관광지", region + "에서 둘러볼 만한 관광지를 모아봤어요");
        }

        if (containsAny(message, "볼거리")) {
            return new DisplayMeta("볼거리", region + "에서 둘러볼 만한 볼거리를 모아봤어요");
        }

        return new DisplayMeta("명소", region + "에서 가볼 만한 명소를 모아봤어요");
    }

    private boolean containsAny(String value, String... keywords) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String normalized = value.toLowerCase();

        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeDisplayDestination(String destination) {
        String region = normalize(destination);

        if (!StringUtils.hasText(region)) {
            return "이 지역";
        }

        if (region.contains("공항")) {
            String[] parts = region.split("\\s+");

            for (String part : parts) {
                if (part.contains("공항")) {
                    return part;
                }
            }
        }

        return region;
    }

    public record DisplayMeta(String displayType, String displayTitle) {
    }
}