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

    private static final String STAY_SUBTYPE_GENERIC = "generic";
    private static final String STAY_SUBTYPE_HOTEL = "hotel";
    private static final String STAY_SUBTYPE_MOTEL = "motel";
    private static final String STAY_SUBTYPE_PENSION = "pension";
    private static final String STAY_SUBTYPE_RESORT = "resort";
    private static final String STAY_SUBTYPE_GUEST_HOUSE = "guesthouse";
    private static final String STAY_SUBTYPE_HANOK = "hanok";
    private static final String STAY_SUBTYPE_POOL_VILLA = "poolvilla";

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

            if (isGyeonggiGwangjuRequest(province, destination, detailArea, district, neighborhood, aliasQueryHint, aliasTargetParent)) {
                if (!isGyeonggiGwangjuAddress(doc)) {
                    continue;
                }
            }

            if (isGwangjuMetroRequest(province, destination, detailArea, district, neighborhood, aliasQueryHint, aliasTargetParent)) {
                if (!isGwangjuMetroAddress(doc)) {
                    continue;
                }
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

    private boolean matchesRequestedType(JsonNode doc, String intent, String staySubtype) {
        if (!"STAY_RECOMMENDATION".equals(intent)) {
            return true;
        }

        if (STAY_SUBTYPE_GENERIC.equals(staySubtype)) {
            return isAnyStayPlace(doc);
        }

        return matchesStaySubtype(doc, staySubtype);
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

    private boolean matchesStaySubtype(JsonNode doc, String staySubtype) {
        String haystack = normalizeAreaName(text(doc, "place_name") + " " + text(doc, "category_name"));

        switch (staySubtype) {
            case STAY_SUBTYPE_MOTEL:
                return haystack.contains("모텔") || haystack.contains("무인텔");
            case STAY_SUBTYPE_HOTEL:
                return haystack.contains("호텔");
            case STAY_SUBTYPE_PENSION:
                return haystack.contains("펜션");
            case STAY_SUBTYPE_RESORT:
                return haystack.contains("리조트");
            case STAY_SUBTYPE_GUEST_HOUSE:
                return haystack.contains("게스트하우스");
            case STAY_SUBTYPE_HANOK:
                return haystack.contains("한옥");
            case STAY_SUBTYPE_POOL_VILLA:
                return haystack.contains("풀빌라");
            default:
                return isAnyStayPlace(doc);
        }
    }

    private int scorePlace(JsonNode doc,
                           String intent,
                           String staySubtype,
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

        if (StringUtils.hasText(detailArea) && containsArea(address, placeName, detailArea)) {
            score += 50;
        }

        if (StringUtils.hasText(neighborhood) && containsArea(address, placeName, neighborhood)) {
            score += 40;
        }

        if (StringUtils.hasText(district) && containsArea(address, placeName, district)) {
            score += 35;
        }

        if (StringUtils.hasText(aliasTargetParent) && containsArea(address, placeName, aliasTargetParent)) {
            score += 30;
        }

        if (StringUtils.hasText(aliasQueryHint) && containsArea(address, placeName, aliasQueryHint)) {
            score += 25;
        }

        if (StringUtils.hasText(destination) && containsArea(address, placeName, destination)) {
            score += 20;
        }

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

            switch (staySubtype) {
                case STAY_SUBTYPE_MOTEL:
                    if (category.contains("모텔")) score += 45;
                    if (placeName.contains("모텔") || placeName.contains("무인텔")) score += 35;
                    break;
                case STAY_SUBTYPE_HOTEL:
                    if (category.contains("호텔")) score += 45;
                    if (placeName.contains("호텔")) score += 35;
                    break;
                case STAY_SUBTYPE_PENSION:
                    if (category.contains("펜션")) score += 45;
                    if (placeName.contains("펜션")) score += 35;
                    break;
                case STAY_SUBTYPE_RESORT:
                    if (category.contains("리조트")) score += 45;
                    if (placeName.contains("리조트")) score += 35;
                    break;
                case STAY_SUBTYPE_GUEST_HOUSE:
                    if (category.contains("게스트하우스")) score += 45;
                    if (placeName.contains("게스트하우스")) score += 35;
                    break;
                case STAY_SUBTYPE_HANOK:
                    if (placeName.contains("한옥") || category.contains("한옥")) score += 45;
                    break;
                case STAY_SUBTYPE_POOL_VILLA:
                    if (placeName.contains("풀빌라") || category.contains("풀빌라")) score += 45;
                    break;
                default:
                    if (category.contains("호텔")) score += 25;
                    if (category.contains("모텔")) score += 25;
                    if (category.contains("펜션")) score += 25;
                    if (category.contains("리조트")) score += 25;
                    if (category.contains("게스트하우스")) score += 25;
                    break;
            }
        }

        if (StringUtils.hasText(text(doc, "road_address_name"))) {
            score += 5;
        }

        if (StringUtils.hasText(text(doc, "phone"))) {
            score += 3;
        }

        return score;
    }

    private boolean containsArea(String address, String placeName, String area) {
        String normalized = normalizeAreaName(area);
        return StringUtils.hasText(normalized)
                && (address.contains(normalized) || placeName.contains(normalized));
    }

    private boolean isRestaurantCuisineMatch(String keyword, String category) {
        switch (keyword) {
            case "돼지국밥":
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
                return category.contains("한식") || category.contains("국밥") || category.contains("탕");

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
                return category.contains("한식") || category.contains("찌개") || category.contains("탕");

            case "백반":
            case "한정식":
            case "비빔밥":
            case "쌈밥":
            case "보리밥":
            case "기사식당":
            case "꼬막비빔밥":
                return category.contains("한식");

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
                return category.contains("한식") || category.contains("고기") || category.contains("구이");

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
                return category.contains("한식") || category.contains("고기") || category.contains("구이");

            case "갈비":
            case "돼지갈비":
            case "소갈비":
            case "양념갈비":
            case "생갈비":
                return category.contains("한식") || category.contains("고기") || category.contains("구이") || category.contains("갈비");

            case "곱창":
            case "대창":
            case "막창":
            case "소막창":
            case "돼지막창":
                return category.contains("한식") || category.contains("고기") || category.contains("구이") || category.contains("막창");

            case "냉면":
            case "밀면":
            case "칼국수":
            case "수제비":
            case "국수":
            case "막국수":
                return category.contains("한식") || category.contains("국수") || category.contains("면");

            case "초밥":
            case "사시미":
            case "오마카세":
                return category.contains("일식") || category.contains("스시") || category.contains("초밥");

            case "횟집":
                return category.contains("횟집") || category.contains("회") || category.contains("일식");

            case "라멘":
            case "우동":
            case "소바":
            case "돈까스":
            case "텐동":
            case "덮밥":
            case "이자카야":
            case "일식":
                return category.contains("일식") || category.contains("스시") || category.contains("초밥");

            case "짜장면":
            case "짬뽕":
            case "탕수육":
            case "마라탕":
            case "마라샹궈":
            case "훠궈":
            case "양꼬치":
            case "딤섬":
            case "중식":
                return category.contains("중식");

            case "파스타":
            case "스테이크":
            case "리조또":
            case "피자":
            case "브런치":
            case "샐러드":
            case "버거":
            case "수제버거":
            case "바베큐":
            case "양식":
                return category.contains("양식");

            case "떡볶이":
            case "김밥":
            case "순대":
            case "튀김":
            case "라볶이":
            case "오뎅":
            case "토스트":
            case "분식":
                return category.contains("분식") || category.contains("한식");

            case "치킨":
            case "닭강정":
                return category.contains("치킨") || category.contains("한식");

            case "카페":
            case "디저트":
            case "베이커리":
            case "빵집":
            case "케이크":
            case "빙수":
            case "와플":
            case "도넛":
            case "아이스크림":
                return category.contains("카페") || category.contains("디저트");

            case "주점":
            case "포차":
            case "호프":
            case "와인바":
            case "칵테일바":
                return category.contains("주점") || category.contains("술집");

            default:
                return false;
        }
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

        String normalizedProvince = normalizeAreaName(province);
        return "경기".equals(normalizedProvince);
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

        return value.trim()
                .replaceAll("\\s+", " ");
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