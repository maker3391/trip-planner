package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class KakaoPlaceQueryBuilder {

    private final RestaurantKeywordService restaurantKeywordService;

    public KakaoPlaceQueryBuilder(RestaurantKeywordService restaurantKeywordService) {
        this.restaurantKeywordService = restaurantKeywordService;
    }

    public List<String> buildQueryCandidates(String intent,
                                             String staySubtype,
                                             String destination,
                                             String detailArea,
                                             String neighborhood,
                                             String district,
                                             String aliasQueryHint,
                                             String aliasTargetName,
                                             String aliasTargetParent,
                                             String rawAreaHint,
                                             String message) {
        LinkedHashSet<String> queries = new LinkedHashSet<>();

        List<String> locationBases = buildLocationBases(
                destination,
                detailArea,
                neighborhood,
                district,
                aliasQueryHint,
                aliasTargetName,
                aliasTargetParent,
                rawAreaHint
        );

        List<String> keywords = buildIntentKeywords(intent, staySubtype, message);

        for (String keyword : keywords) {
            for (String base : locationBases) {
                queries.add(base + " " + keyword);
            }
        }

        return new ArrayList<>(queries);
    }

    private List<String> buildLocationBases(String destination,
                                            String detailArea,
                                            String neighborhood,
                                            String district,
                                            String aliasQueryHint,
                                            String aliasTargetName,
                                            String aliasTargetParent,
                                            String rawAreaHint) {
        LinkedHashSet<String> bases = new LinkedHashSet<>();

        addExpandedArea(bases, detailArea);
        addExpandedArea(bases, neighborhood);
        addExpandedArea(bases, aliasTargetName);
        addExpandedArea(bases, rawAreaHint);
        addExpandedArea(bases, district);
        addExpandedArea(bases, aliasTargetParent);
        addExpandedArea(bases, aliasQueryHint);
        addExpandedArea(bases, destination);

        return new ArrayList<>(bases);
    }

    private void addExpandedArea(Set<String> bases, String area) {
        if (!StringUtils.hasText(area)) {
            return;
        }

        for (String variant : expandAreaVariants(normalizeDisplayArea(area))) {
            if (StringUtils.hasText(variant)) {
                bases.add(variant);
            }
        }
    }

    private List<String> expandAreaVariants(String area) {
        LinkedHashSet<String> result = new LinkedHashSet<>();

        if (!StringUtils.hasText(area)) {
            return new ArrayList<>();
        }

        String value = area.trim();

        switch (value) {
            case "전라도":
                result.add("전남");
                result.add("전북");
                result.add("전라남도");
                result.add("전라북도");
                return new ArrayList<>(result);
            case "경상도":
                result.add("경남");
                result.add("경북");
                result.add("경상남도");
                result.add("경상북도");
                return new ArrayList<>(result);
            case "충청도":
                result.add("충남");
                result.add("충북");
                result.add("충청남도");
                result.add("충청북도");
                return new ArrayList<>(result);
            default:
                result.add(value);
                break;
        }

        switch (value) {
            case "경상북":
                result.add("경북");
                result.add("경상북도");
                break;
            case "경상남":
                result.add("경남");
                result.add("경상남도");
                break;
            case "전라북":
                result.add("전북");
                result.add("전라북도");
                break;
            case "전라남":
                result.add("전남");
                result.add("전라남도");
                break;
            case "충청북":
                result.add("충북");
                result.add("충청북도");
                break;
            case "충청남":
                result.add("충남");
                result.add("충청남도");
                break;
            case "강원":
            case "강원도":
                result.add("강원");
                result.add("강원도");
                result.add("강원특별자치도");
                break;
            case "제주":
            case "제주도":
                result.add("제주");
                result.add("제주도");
                result.add("제주특별자치도");
                break;
            case "경기":
            case "경기도":
                result.add("경기");
                result.add("경기도");
                break;
            case "서울":
            case "서울시":
                result.add("서울");
                result.add("서울특별시");
                break;
            case "부산":
            case "부산시":
                result.add("부산");
                result.add("부산광역시");
                break;
            case "대구":
            case "대구시":
                result.add("대구");
                result.add("대구광역시");
                break;
            case "인천":
            case "인천시":
                result.add("인천");
                result.add("인천광역시");
                break;
            case "광주":
                result.add("광주");
                result.add("광주광역시");
                break;
            case "광주시":
                result.add("광주시");
                result.add("경기 광주시");
                result.add("경기도 광주시");
                break;
            case "대전":
            case "대전시":
                result.add("대전");
                result.add("대전광역시");
                break;
            case "울산":
            case "울산시":
                result.add("울산");
                result.add("울산광역시");
                break;
            case "세종":
            case "세종시":
                result.add("세종");
                result.add("세종특별자치시");
                break;
            default:
                break;
        }

        return new ArrayList<>(result);
    }

    private List<String> buildIntentKeywords(String intent, String staySubtype, String message) {
        LinkedHashSet<String> keywords = new LinkedHashSet<>();

        if ("ATTRACTION_RECOMMENDATION".equals(intent)) {
            throw new IllegalArgumentException(
                    "ATTRACTION_RECOMMENDATION은 AttractionRecommendationService에서 처리해야 합니다."
            );
        }

        if ("RESTAURANT_RECOMMENDATION".equals(intent)) {
            List<String> foodKeywords = restaurantKeywordService.extractRestaurantFoodKeywords(message);

            for (String foodKeyword : foodKeywords) {
                keywords.addAll(restaurantKeywordService.expandRestaurantKeywordVariants(foodKeyword));
            }

            if (restaurantKeywordService.isCafeFocusedRequest(message)) {
                keywords.add("카페");
                keywords.add("디저트");
                keywords.add("베이커리");
                return new ArrayList<>(keywords);
            }

            if (restaurantKeywordService.isPubFocusedRequest(message)) {
                keywords.add("술집");
                keywords.add("주점");
                keywords.add("포차");
                return new ArrayList<>(keywords);
            }

            keywords.add("맛집");
            keywords.add("식당");
            keywords.add("밥집");

            if (foodKeywords.isEmpty()) {
                keywords.add("한식");
            }

            return new ArrayList<>(keywords);
        }

        switch (staySubtype) {
            case "motel":
                keywords.add("모텔");
                keywords.add("무인텔");
                keywords.add("숙박");
                break;
            case "hotel":
                keywords.add("호텔");
                keywords.add("숙소");
                break;
            case "pension":
                keywords.add("펜션");
                keywords.add("숙소");
                break;
            case "resort":
                keywords.add("리조트");
                keywords.add("숙소");
                break;
            case "guesthouse":
                keywords.add("게스트하우스");
                keywords.add("숙소");
                break;
            case "hanok":
                keywords.add("한옥스테이");
                keywords.add("한옥 숙소");
                keywords.add("숙소");
                break;
            case "poolvilla":
                keywords.add("풀빌라");
                keywords.add("리조트");
                keywords.add("숙소");
                break;
            default:
                keywords.add("숙소");
                keywords.add("숙박");
                keywords.add("호텔");
                keywords.add("모텔");
                keywords.add("펜션");
                keywords.add("게스트하우스");
                break;
        }

        return new ArrayList<>(keywords);
    }

    public List<String> extractRestaurantFoodKeywords(String message) {
        return restaurantKeywordService.extractRestaurantFoodKeywords(message);
    }

    private String normalizeDisplayArea(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim().replaceAll("\\s+", " ");
    }
}