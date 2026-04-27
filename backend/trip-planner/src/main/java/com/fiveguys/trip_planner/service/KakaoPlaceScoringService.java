package com.fiveguys.trip_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class KakaoPlaceScoringService {

    private final KakaoPlaceMapper kakaoPlaceMapper;
    private final RestaurantKeywordService restaurantKeywordService;

    public KakaoPlaceScoringService(KakaoPlaceMapper kakaoPlaceMapper,
                                    RestaurantKeywordService restaurantKeywordService) {
        this.kakaoPlaceMapper = kakaoPlaceMapper;
        this.restaurantKeywordService = restaurantKeywordService;
    }

    public List<RecommendationCandidate> filterAndRank(List<JsonNode> docs,
                                                       String intent,
                                                       String staySubtype,
                                                       String province,
                                                       String destination,
                                                       String detailArea,
                                                       String neighborhood,
                                                       String district,
                                                       String aliasQueryHint,
                                                       String aliasTargetParent,
                                                       String message) {
        return filterAndRank(
                docs,
                intent,
                StaySubtype.from(staySubtype),
                province,
                destination,
                detailArea,
                neighborhood,
                district,
                aliasQueryHint,
                aliasTargetParent,
                message
        );
    }

    public List<RecommendationCandidate> filterAndRank(List<JsonNode> docs,
                                                       String intent,
                                                       StaySubtype staySubtype,
                                                       String province,
                                                       String destination,
                                                       String detailArea,
                                                       String neighborhood,
                                                       String district,
                                                       String aliasQueryHint,
                                                       String aliasTargetParent,
                                                       String message) {
        if (!"RESTAURANT_RECOMMENDATION".equals(intent)
                && !"STAY_RECOMMENDATION".equals(intent)) {
            throw new IllegalArgumentException("지원하지 않는 place intent입니다: " + intent);
        }

        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<RecommendationCandidate> scored = new ArrayList<>();
        List<String> foodKeywords = restaurantKeywordService.extractRestaurantFoodKeywords(message);
        boolean cafeFocused = restaurantKeywordService.isCafeFocusedRequest(message);
        boolean pubFocused = restaurantKeywordService.isPubFocusedRequest(message);

        for (JsonNode doc : docs) {
            if (!matchesRequestedType(doc, intent, staySubtype)) {
                continue;
            }

            String category = normalizeAreaName(text(doc, "category_name"));

            if ("RESTAURANT_RECOMMENDATION".equals(intent)) {
                if (!cafeFocused && !pubFocused && category.contains("카페")) {
                    continue;
                }

                if (!cafeFocused && !pubFocused && category.contains("주점")) {
                    continue;
                }

                if (cafeFocused && !category.contains("카페") && !category.contains("디저트")) {
                    continue;
                }

                if (pubFocused && !category.contains("주점") && !category.contains("술집")) {
                    continue;
                }
            }

            if (isGyeonggiGwangjuRequest(province, destination, detailArea, district, neighborhood, aliasQueryHint, aliasTargetParent)
                    && !isGyeonggiGwangjuAddress(doc)) {
                continue;
            }

            if (isGwangjuMetroRequest(province, destination, detailArea, district, neighborhood, aliasQueryHint, aliasTargetParent)
                    && !isGwangjuMetroAddress(doc)) {
                continue;
            }

            int score = scorePlace(
                    doc,
                    intent,
                    staySubtype,
                    destination,
                    detailArea,
                    neighborhood,
                    district,
                    aliasQueryHint,
                    aliasTargetParent,
                    foodKeywords
            );

            if (score <= 0) {
                continue;
            }

            String name = text(doc, "place_name");
            String address = kakaoPlaceMapper.resolveAddress(doc);
            String key = (name + "|" + address).trim();

            if (!seen.add(key)) {
                continue;
            }

            scored.add(new RecommendationCandidate(doc, score));
        }

        scored.sort(Comparator.comparingInt(RecommendationCandidate::score).reversed());
        return scored;
    }

    private boolean matchesRequestedType(JsonNode doc, String intent, StaySubtype staySubtype) {
        if (!"STAY_RECOMMENDATION".equals(intent)) {
            return true;
        }

        StaySubtype resolvedSubtype = staySubtype == null ? StaySubtype.GENERIC : staySubtype;

        if (resolvedSubtype == StaySubtype.GENERIC) {
            return isAnyStayPlace(doc);
        }

        return matchesStaySubtype(doc, resolvedSubtype);
    }

    private boolean isAnyStayPlace(JsonNode doc) {
        String haystack = normalizeAreaName(text(doc, "place_name") + " " + text(doc, "category_name"));

        return haystack.contains("숙박")
                || haystack.contains("호텔")
                || haystack.contains("모텔")
                || haystack.contains("무인텔")
                || haystack.contains("펜션")
                || haystack.contains("리조트")
                || haystack.contains("게스트하우스")
                || haystack.contains("한옥")
                || haystack.contains("풀빌라");
    }

    private boolean matchesStaySubtype(JsonNode doc, StaySubtype staySubtype) {
        String haystack = normalizeAreaName(text(doc, "place_name") + " " + text(doc, "category_name"));

        return switch (staySubtype) {
            case MOTEL -> haystack.contains("모텔") || haystack.contains("무인텔");
            case HOTEL -> haystack.contains("호텔");
            case PENSION -> haystack.contains("펜션");
            case RESORT -> haystack.contains("리조트");
            case GUEST_HOUSE -> haystack.contains("게스트하우스");
            case HANOK -> haystack.contains("한옥");
            case POOL_VILLA -> haystack.contains("풀빌라");
            case OCEAN_VIEW -> haystack.contains("오션뷰")
                    || haystack.contains("바다뷰")
                    || haystack.contains("해변")
                    || haystack.contains("비치")
                    || haystack.contains("호텔")
                    || haystack.contains("리조트")
                    || haystack.contains("펜션");

            case EMOTIONAL -> haystack.contains("감성")
                    || haystack.contains("스테이")
                    || haystack.contains("호텔")
                    || haystack.contains("펜션")
                    || haystack.contains("게스트하우스")
                    || haystack.contains("한옥");

            case BUDGET -> haystack.contains("호텔")
                    || haystack.contains("모텔")
                    || haystack.contains("게스트하우스")
                    || haystack.contains("숙박")
                    || haystack.contains("숙소");
            default -> isAnyStayPlace(doc);
        };
    }

    private int scorePlace(JsonNode doc,
                           String intent,
                           StaySubtype staySubtype,
                           String destination,
                           String detailArea,
                           String neighborhood,
                           String district,
                           String aliasQueryHint,
                           String aliasTargetParent,
                           List<String> foodKeywords) {
        int score = 0;

        String placeName = normalizeAreaName(text(doc, "place_name"));
        String address = normalizeAreaName(kakaoPlaceMapper.resolveAddress(doc));
        String category = normalizeAreaName(text(doc, "category_name"));

        if (StringUtils.hasText(detailArea) && containsArea(address, placeName, detailArea)) score += 50;
        if (StringUtils.hasText(neighborhood) && containsArea(address, placeName, neighborhood)) score += 40;
        if (StringUtils.hasText(district) && containsArea(address, placeName, district)) score += 35;
        if (StringUtils.hasText(aliasTargetParent) && containsArea(address, placeName, aliasTargetParent)) score += 30;
        if (StringUtils.hasText(aliasQueryHint) && containsArea(address, placeName, aliasQueryHint)) score += 25;
        if (StringUtils.hasText(destination) && containsArea(address, placeName, destination)) score += 20;

        if ("RESTAURANT_RECOMMENDATION".equals(intent)) {
            if (category.contains("음식점")) score += 10;
            if (category.contains("한식")) score += 8;
            if (category.contains("중식")) score += 8;
            if (category.contains("일식")) score += 8;
            if (category.contains("양식")) score += 8;
            if (category.contains("분식")) score += 8;
            if (category.contains("카페")) score += 6;
            if (category.contains("주점")) score += 6;
            if (category.contains("고기") || category.contains("구이")) score += 10;
            if (category.contains("국밥") || category.contains("찌개") || category.contains("탕")) score += 10;

            for (String keyword : foodKeywords) {
                String normalizedKeyword = normalizeAreaName(keyword);

                if (!StringUtils.hasText(normalizedKeyword)) {
                    continue;
                }

                if (placeName.contains(normalizedKeyword)) {
                    score += 45;
                }

                if (category.contains(normalizedKeyword)) {
                    score += 35;
                }

                if (isRestaurantCuisineMatch(normalizedKeyword, category)) {
                    score += 15;
                }
            }
        } else {
            if (category.contains("숙박")) score += 35;

            StaySubtype resolvedSubtype = staySubtype == null ? StaySubtype.GENERIC : staySubtype;

            switch (resolvedSubtype) {
                case MOTEL -> {
                    if (category.contains("모텔")) score += 45;
                    if (placeName.contains("모텔") || placeName.contains("무인텔")) score += 35;
                }
                case HOTEL -> {
                    if (category.contains("호텔")) score += 45;
                    if (placeName.contains("호텔")) score += 35;
                }
                case PENSION -> {
                    if (category.contains("펜션")) score += 45;
                    if (placeName.contains("펜션")) score += 35;
                }
                case RESORT -> {
                    if (category.contains("리조트")) score += 45;
                    if (placeName.contains("리조트")) score += 35;
                }
                case GUEST_HOUSE -> {
                    if (category.contains("게스트하우스")) score += 45;
                    if (placeName.contains("게스트하우스")) score += 35;
                }
                case HANOK -> {
                    if (placeName.contains("한옥") || category.contains("한옥")) score += 45;
                }
                case POOL_VILLA -> {
                    if (placeName.contains("풀빌라") || category.contains("풀빌라")) score += 45;
                }
                case OCEAN_VIEW -> {
                    if (placeName.contains("오션뷰") || placeName.contains("바다뷰")) score += 60;
                    if (placeName.contains("해변") || placeName.contains("비치")) score += 35;
                    if (category.contains("호텔") || category.contains("리조트") || category.contains("펜션")) score += 25;
                }
                case EMOTIONAL -> {
                    if (placeName.contains("감성")) score += 55;
                    if (placeName.contains("스테이")) score += 35;
                    if (placeName.contains("한옥")) score += 25;
                    if (category.contains("호텔") || category.contains("펜션") || category.contains("게스트하우스")) score += 20;
                }
                case BUDGET -> {
                    if (placeName.contains("가성비") || placeName.contains("저렴")) score += 50;
                    if (category.contains("모텔")) score += 35;
                    if (category.contains("호텔")) score += 25;
                    if (category.contains("게스트하우스")) score += 25;
                }
                default -> {
                    if (category.contains("호텔")) score += 25;
                    if (category.contains("모텔")) score += 25;
                    if (category.contains("펜션")) score += 25;
                    if (category.contains("리조트")) score += 25;
                    if (category.contains("게스트하우스")) score += 25;
                }
            }
        }

        if (StringUtils.hasText(text(doc, "road_address_name"))) score += 5;
        if (StringUtils.hasText(text(doc, "phone"))) score += 3;

        return score;
    }

    private boolean containsArea(String address, String placeName, String area) {
        String normalized = normalizeAreaName(area);
        return StringUtils.hasText(normalized)
                && (address.contains(normalized) || placeName.contains(normalized));
    }

    private boolean isRestaurantCuisineMatch(String keyword, String category) {
        return switch (keyword) {
            case "돼지국밥", "국밥", "순대국", "설렁탕", "곰탕", "갈비탕", "해장국", "감자탕", "추어탕", "삼계탕", "백숙" ->
                    category.contains("한식") || category.contains("국밥") || category.contains("탕");

            case "김치찌개", "된장찌개", "청국장", "순두부", "부대찌개", "동태찌개", "매운탕", "전골", "곱도리탕", "닭볶음탕", "아구찜", "해물탕", "해물찜", "샤브샤브", "닭한마리" ->
                    category.contains("한식") || category.contains("찌개") || category.contains("탕");

            case "백반", "한정식", "비빔밥", "쌈밥", "보리밥", "기사식당", "꼬막비빔밥" ->
                    category.contains("한식");

            case "불고기", "삼겹살", "목살", "항정살", "가브리살", "족발", "보쌈", "닭갈비", "제육볶음", "오리구이", "장어구이", "생선구이", "게장", "육회", "육사시미", "닭발", "쭈꾸미", "오징어볶음", "낙곱새", "고기집", "흑돼지" ->
                    category.contains("한식") || category.contains("고기") || category.contains("구이");

            case "한우", "소고기", "꽃등심", "등심", "안심", "차돌박이", "토시살", "살치살", "안창살", "갈비살", "업진살", "제비추리", "부채살" ->
                    category.contains("한식") || category.contains("고기") || category.contains("구이");

            case "갈비", "돼지갈비", "소갈비", "양념갈비", "생갈비" ->
                    category.contains("한식") || category.contains("고기") || category.contains("구이") || category.contains("갈비");

            case "곱창", "대창", "막창", "소막창", "돼지막창" ->
                    category.contains("한식") || category.contains("고기") || category.contains("구이") || category.contains("막창");

            case "냉면", "밀면", "칼국수", "수제비", "국수", "막국수" ->
                    category.contains("한식") || category.contains("국수") || category.contains("면");

            case "초밥", "사시미", "오마카세" ->
                    category.contains("일식") || category.contains("스시") || category.contains("초밥");

            case "횟집" ->
                    category.contains("횟집") || category.contains("회") || category.contains("일식");

            case "라멘", "우동", "소바", "돈까스", "텐동", "덮밥", "이자카야", "일식" ->
                    category.contains("일식") || category.contains("스시") || category.contains("초밥");

            case "짜장면", "짬뽕", "탕수육", "마라탕", "마라샹궈", "훠궈", "양꼬치", "딤섬", "중식" ->
                    category.contains("중식");

            case "파스타", "스테이크", "리조또", "피자", "브런치", "샐러드", "버거", "수제버거", "바베큐", "양식" ->
                    category.contains("양식");

            case "떡볶이", "김밥", "순대", "튀김", "라볶이", "오뎅", "토스트", "분식" ->
                    category.contains("분식") || category.contains("한식");

            case "치킨", "닭강정" ->
                    category.contains("치킨") || category.contains("한식");

            case "카페", "디저트", "베이커리", "빵집", "케이크", "빙수", "와플", "도넛", "아이스크림" ->
                    category.contains("카페") || category.contains("디저트");

            case "주점", "포차", "호프", "와인바", "칵테일바" ->
                    category.contains("주점") || category.contains("술집");

            default -> false;
        };
    }

    private boolean isGyeonggiGwangjuRequest(String province,
                                             String destination,
                                             String detailArea,
                                             String district,
                                             String neighborhood,
                                             String aliasQueryHint,
                                             String aliasTargetParent) {
        boolean hasGwangjuCity =
                isGyeonggiGwangjuArea(destination)
                        || isGyeonggiGwangjuArea(detailArea)
                        || isGyeonggiGwangjuArea(district)
                        || isGyeonggiGwangjuArea(neighborhood)
                        || isGyeonggiGwangjuArea(aliasQueryHint)
                        || isGyeonggiGwangjuArea(aliasTargetParent);

        if (!hasGwangjuCity) {
            return false;
        }

        return "경기".equals(normalizeAreaName(province));
    }

    private boolean isGwangjuMetroRequest(String province,
                                          String destination,
                                          String detailArea,
                                          String district,
                                          String neighborhood,
                                          String aliasQueryHint,
                                          String aliasTargetParent) {
        String normalizedProvince = normalizeAreaName(province);

        if ("광주".equals(normalizedProvince)) {
            return true;
        }

        boolean hasMetroHint =
                isGwangjuMetroArea(destination)
                        || isGwangjuMetroArea(detailArea)
                        || isGwangjuMetroArea(district)
                        || isGwangjuMetroArea(neighborhood)
                        || isGwangjuMetroArea(aliasQueryHint)
                        || isGwangjuMetroArea(aliasTargetParent);

        return hasMetroHint && !"경기".equals(normalizedProvince);
    }

    private boolean isGyeonggiGwangjuArea(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String normalized = normalizeDisplayArea(value);

        return "광주시".equals(normalized)
                || "경기 광주시".equals(normalized)
                || "경기도 광주시".equals(normalized);
    }

    private boolean isGwangjuMetroArea(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String normalized = normalizeDisplayArea(value);

        return "광주".equals(normalized)
                || "광주광역시".equals(normalized)
                || "광주 시".equals(normalized);
    }

    private boolean isGyeonggiGwangjuAddress(JsonNode doc) {
        String normalizedAddress = normalizeDisplayArea(kakaoPlaceMapper.resolveAddress(doc));
        return normalizedAddress.startsWith("경기 광주시")
                || normalizedAddress.startsWith("경기도 광주시");
    }

    private boolean isGwangjuMetroAddress(JsonNode doc) {
        String normalizedAddress = normalizeDisplayArea(kakaoPlaceMapper.resolveAddress(doc));
        return normalizedAddress.startsWith("광주 ")
                || normalizedAddress.startsWith("광주광역시 ");
    }

    private String normalizeDisplayArea(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeAreaName(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim()
                .replaceAll("\\s+", "")
                .replace("특별자치도", "")
                .replace("특별자치시", "")
                .replace("광역시", "")
                .replace("특별시", "");
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isNull()) {
            return "";
        }

        return node.get(fieldName).asText("");
    }

    public record RecommendationCandidate(JsonNode doc, int score) {
    }
}