package com.fiveguys.trip_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fiveguys.trip_planner.client.KakaoLocalClient;
import com.fiveguys.trip_planner.dto.ChatRequest;
import com.fiveguys.trip_planner.dto.ParsedRegion;
import com.fiveguys.trip_planner.exception.LlmCallException;
import com.fiveguys.trip_planner.response.ChatResponse;
import com.fiveguys.trip_planner.response.RecommendationContentResponse;
import com.fiveguys.trip_planner.response.RecommendationItemResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class KakaoPlaceRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(KakaoPlaceRecommendationService.class);
    private static final int MAX_ITEMS = 5;
    private static final int MAX_COLLECT_SIZE = 20;

    private final KakaoLocalClient kakaoLocalClient;
    private final DetailAreaParsingService detailAreaParsingService;
    private final RecommendationIntentResolverService intentResolverService;
    private final RegionParsingService regionParsingService;
    private final RecommendationCacheService recommendationCacheService;
    private final RecommendationCacheKeyGenerator cacheKeyGenerator;

    public KakaoPlaceRecommendationService(KakaoLocalClient kakaoLocalClient,
                                           DetailAreaParsingService detailAreaParsingService,
                                           RecommendationIntentResolverService intentResolverService,
                                           RegionParsingService regionParsingService,
                                           RecommendationCacheService recommendationCacheService,
                                           RecommendationCacheKeyGenerator cacheKeyGenerator) {
        this.kakaoLocalClient = kakaoLocalClient;
        this.detailAreaParsingService = detailAreaParsingService;
        this.intentResolverService = intentResolverService;
        this.regionParsingService = regionParsingService;
        this.recommendationCacheService = recommendationCacheService;
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    public ChatResponse recommend(ChatRequest request) {
        String message = request.getMessage();

        String intent = intentResolverService.resolve(message);
        String detailArea = detailAreaParsingService.extractDetailArea(message);
        ParsedRegion parsedRegion = regionParsingService.parse(message, "");

        validateRegionStrict(parsedRegion, detailArea);

        String destination = resolveDestination(parsedRegion, detailArea);
        String district = parsedRegion == null ? null : parsedRegion.getDistrict();
        String neighborhood = parsedRegion == null ? null : parsedRegion.getNeighborhood();

        if (!StringUtils.hasText(destination)) {
            throw new LlmCallException("지역은 반드시 앞에 포함해야 합니다. 예: 부산 중구 맛집 추천, 서울 성수동 카페 추천");
        }

        String cacheKey = cacheKeyGenerator.generate(message);

        ChatResponse cached = recommendationCacheService.get(cacheKey);
        if (cached != null) {
            log.info("[KAKAO CACHE HIT] key={}", cacheKey);
            return cached;
        }

        List<String> queryCandidates = buildQueryCandidates(
                intent,
                destination,
                detailArea,
                neighborhood,
                district,
                message
        );

        log.info("[KAKAO QUERY CANDIDATES] {}", queryCandidates);

        List<JsonNode> collectedDocs = new ArrayList<>();

        for (String query : queryCandidates) {
            JsonNode root = kakaoLocalClient.searchKeyword(query);
            List<JsonNode> docs = extractDocuments(root);

            if (!docs.isEmpty()) {
                collectedDocs.addAll(docs);
            }

            if (collectedDocs.size() >= MAX_COLLECT_SIZE) {
                break;
            }
        }

        if (collectedDocs.isEmpty()) {
            throw new LlmCallException("장소 검색 결과가 없습니다: " + buildDisplayDestination(destination, detailArea, neighborhood, district));
        }

        List<RecommendationItemResponse> items = filterScoreSortAndMap(
                collectedDocs,
                intent,
                detailArea,
                neighborhood,
                district
        );

        if (items.isEmpty()) {
            throw new LlmCallException("추천 가능한 결과가 없습니다: " + buildDisplayDestination(destination, detailArea, neighborhood, district));
        }

        ChatResponse response = new ChatResponse(
                message,
                intent,
                buildDisplayDestination(destination, detailArea, neighborhood, district),
                null,
                new RecommendationContentResponse(
                        new ArrayList<>(),
                        items
                )
        );

        recommendationCacheService.put(cacheKey, response, resolveTtl(intent));
        return response;
    }

    private void validateRegionStrict(ParsedRegion parsedRegion, String detailArea) {
        if (parsedRegion == null) {
            throw new LlmCallException("지역은 반드시 앞에 포함해야 합니다. 예: 부산 중구 맛집 추천, 서울 성수동 카페 추천");
        }

        boolean hasProvince = StringUtils.hasText(parsedRegion.getProvince());
        boolean hasCity = StringUtils.hasText(parsedRegion.getCity());
        boolean hasDistrict = StringUtils.hasText(parsedRegion.getDistrict());
        boolean hasNeighborhood = StringUtils.hasText(parsedRegion.getNeighborhood());
        boolean hasDetailArea = StringUtils.hasText(detailArea);

        if (!hasProvince && !hasCity) {
            throw new LlmCallException("지역은 반드시 앞에 포함해야 합니다. 예: 부산 중구 맛집 추천, 서울 성수동 카페 추천");
        }

        if ((hasDistrict || hasNeighborhood || hasDetailArea) && !hasCity && !hasProvince) {
            throw new LlmCallException("지역은 반드시 앞에 포함해야 합니다. 예: 부산 중구 맛집 추천, 서울 성수동 카페 추천");
        }
    }

    private String resolveDestination(ParsedRegion parsedRegion, String detailArea) {
        if (StringUtils.hasText(detailArea)) {
            String parentCity = detailAreaParsingService.resolveParentCity(detailArea);
            if (StringUtils.hasText(parentCity)) {
                return parentCity;
            }
        }

        if (parsedRegion == null) {
            return null;
        }

        if (StringUtils.hasText(parsedRegion.getCity())) {
            return parsedRegion.getCity();
        }

        if (StringUtils.hasText(parsedRegion.getProvince())) {
            return parsedRegion.getProvince();
        }

        return null;
    }

    private List<String> buildQueryCandidates(String intent,
                                              String destination,
                                              String detailArea,
                                              String neighborhood,
                                              String district,
                                              String message) {
        Set<String> candidates = new LinkedHashSet<>();
        List<String> locationBases = buildLocationBases(destination, detailArea, neighborhood, district);

        if ("RESTAURANT_RECOMMENDATION".equals(intent)) {
            for (String base : locationBases) {
                if (containsKeyword(message, "카페", "cafe")) {
                    candidates.add(base + " 카페");
                } else if (containsKeyword(message, "술집")) {
                    candidates.add(base + " 술집");
                    candidates.add(base + " 주점");
                } else if (containsKeyword(message, "밥집")) {
                    candidates.add(base + " 밥집");
                    candidates.add(base + " 식당");
                    candidates.add(base + " 한식");
                } else {
                    candidates.add(base + " 맛집");
                    candidates.add(base + " 식당");
                    candidates.add(base + " 밥집");
                    candidates.add(base + " 한식");
                    candidates.add(base + " 국밥");
                    candidates.add(base + " 백반");
                    candidates.add(base + " 분식");
                }
            }
        } else if ("STAY_RECOMMENDATION".equals(intent)) {
            String primaryStayKeyword = resolveStayKeyword(message);

            for (String base : locationBases) {
                candidates.add(base + " " + primaryStayKeyword);
                candidates.add(base + " 숙소");
                candidates.add(base + " 숙박");
                candidates.add(base + " 호텔");
                candidates.add(base + " 모텔");
                candidates.add(base + " 무인텔");
                candidates.add(base + " 펜션");
                candidates.add(base + " 리조트");
                candidates.add(base + " 게스트하우스");
                candidates.add(base + " 민박");
            }
        }

        return new ArrayList<>(candidates);
    }

    private List<String> buildLocationBases(String destination,
                                            String detailArea,
                                            String neighborhood,
                                            String district) {
        List<String> bases = new ArrayList<>();

        if (StringUtils.hasText(detailArea)) {
            bases.add(destination + " " + detailArea);
        }

        if (StringUtils.hasText(neighborhood)) {
            bases.add(destination + " " + neighborhood);
        }

        if (StringUtils.hasText(district)) {
            bases.add(destination + " " + district);
        }

        bases.add(destination);

        return dedupStrings(bases);
    }

    private List<String> dedupStrings(List<String> values) {
        Set<String> seen = new LinkedHashSet<>();
        List<String> result = new ArrayList<>();

        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }

            String normalized = value.trim().replaceAll("\\s+", " ");
            if (seen.add(normalized)) {
                result.add(normalized);
            }
        }

        return result;
    }

    private List<JsonNode> extractDocuments(JsonNode root) {
        List<JsonNode> result = new ArrayList<>();
        JsonNode docs = root.path("documents");

        if (!docs.isArray()) {
            return result;
        }

        for (JsonNode doc : docs) {
            result.add(doc);
        }

        return result;
    }

    private List<RecommendationItemResponse> filterScoreSortAndMap(List<JsonNode> docs,
                                                                   String intent,
                                                                   String detailArea,
                                                                   String neighborhood,
                                                                   String district) {
        List<ScoredPlace> scoredPlaces = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (JsonNode doc : docs) {
            if (!isAllowedCategory(doc, intent)) {
                continue;
            }

            String name = clean(doc.path("place_name").asText());
            String address = chooseAddress(doc);
            String placeUrl = clean(doc.path("place_url").asText());
            String category = clean(doc.path("category_name").asText());

            if (!StringUtils.hasText(name)) {
                continue;
            }

            if (looksLikeNoise(name, category)) {
                continue;
            }

            String dedupKey = buildDedupKey(name, address);
            if (!seen.add(dedupKey)) {
                continue;
            }

            int score = score(doc, intent, detailArea, neighborhood, district);

            scoredPlaces.add(new ScoredPlace(name, address, placeUrl, category, score));
        }

        scoredPlaces.sort(Comparator.comparingInt(ScoredPlace::score).reversed());

        List<RecommendationItemResponse> result = new ArrayList<>();
        for (ScoredPlace place : scoredPlaces) {
            result.add(new RecommendationItemResponse(
                    place.name(),
                    place.address(),
                    place.placeUrl(),
                    place.category()
            ));

            if (result.size() >= MAX_ITEMS) {
                break;
            }
        }

        return result;
    }

    private boolean isAllowedCategory(JsonNode doc, String intent) {
        String categoryGroupCode = clean(doc.path("category_group_code").asText());
        String categoryName = clean(doc.path("category_name").asText());

        if ("RESTAURANT_RECOMMENDATION".equals(intent)) {
            return "FD6".equals(categoryGroupCode)
                    || "CE7".equals(categoryGroupCode)
                    || containsKeyword(categoryName, "음식점", "카페", "주점", "술집", "한식", "중식", "일식", "양식");
        }

        if ("STAY_RECOMMENDATION".equals(intent)) {
            return containsKeyword(categoryName, "숙박", "호텔", "모텔", "펜션", "리조트", "게스트하우스", "민박");
        }

        return true;
    }

    private int score(JsonNode doc,
                      String intent,
                      String detailArea,
                      String neighborhood,
                      String district) {
        int score = 0;

        String name = clean(doc.path("place_name").asText());
        String address = chooseAddress(doc);
        String category = clean(doc.path("category_name").asText());

        if (StringUtils.hasText(detailArea)) {
            if (containsKeyword(address, detailArea)) score += 12;
            if (containsKeyword(name, detailArea)) score += 8;
        }

        if (StringUtils.hasText(neighborhood)) {
            if (containsKeyword(address, neighborhood)) score += 10;
            if (containsKeyword(name, neighborhood)) score += 6;
        }

        if (StringUtils.hasText(district)) {
            if (containsKeyword(address, district)) score += 7;
            if (containsKeyword(name, district)) score += 4;
        }

        if ("RESTAURANT_RECOMMENDATION".equals(intent)) {
            if (containsKeyword(category, "음식점", "한식", "중식", "일식", "양식")) score += 5;
            if (containsKeyword(category, "카페") && !containsKeyword(name, "카페")) score -= 2;
        }

        if ("STAY_RECOMMENDATION".equals(intent)) {
            if (containsKeyword(category, "호텔", "모텔", "펜션", "리조트", "게스트하우스", "숙박")) score += 5;
        }

        if (StringUtils.hasText(doc.path("road_address_name").asText())) {
            score += 2;
        }

        if (StringUtils.hasText(doc.path("phone").asText())) {
            score += 1;
        }

        if (StringUtils.hasText(name) && name.length() <= 20) {
            score += 1;
        }

        return score;
    }

    private boolean looksLikeNoise(String name, String category) {
        if (!StringUtils.hasText(name)) {
            return true;
        }

        if (name.length() > 35) {
            return true;
        }

        return containsKeyword(name, "주차장", "관리사무소", "행정복지센터", "주민센터", "버스정류장")
                || containsKeyword(category, "교통", "공공기관", "행정기관");
    }

    private String chooseAddress(JsonNode doc) {
        String roadAddress = clean(doc.path("road_address_name").asText());
        if (StringUtils.hasText(roadAddress)) {
            return roadAddress;
        }
        return clean(doc.path("address_name").asText());
    }

    private String buildDedupKey(String name, String address) {
        return (clean(name) + "|" + clean(address))
                .toLowerCase()
                .replaceAll("[\\s\\-_/()\\[\\],.]", "");
    }

    private String buildDisplayDestination(String destination,
                                           String detailArea,
                                           String neighborhood,
                                           String district) {
        if (StringUtils.hasText(detailArea)) {
            return destination + " " + detailArea;
        }

        if (StringUtils.hasText(neighborhood)) {
            return destination + " " + neighborhood;
        }

        if (StringUtils.hasText(district)) {
            return destination + " " + district;
        }

        return destination;
    }

    private String resolveStayKeyword(String message) {
        String value = message == null ? "" : message.toLowerCase();

        if (value.contains("모텔")) return "모텔";
        if (value.contains("펜션")) return "펜션";
        if (value.contains("게스트하우스")) return "게스트하우스";
        if (value.contains("리조트")) return "리조트";
        if (value.contains("호스텔")) return "호스텔";
        if (value.contains("민박")) return "민박";
        if (value.contains("풀빌라")) return "풀빌라";
        if (value.contains("한옥스테이")) return "한옥스테이";
        if (value.contains("에어비앤비")) return "에어비앤비";

        return "호텔";
    }

    private boolean containsKeyword(String value, String... keywords) {
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

    private String clean(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private record ScoredPlace(String name, String address, String placeUrl, String category, int score) {
    }

    private Duration resolveTtl(String intent) {
        if ("RESTAURANT_RECOMMENDATION".equals(intent)) {
            return Duration.ofHours(2);
        }

        if ("STAY_RECOMMENDATION".equals(intent)) {
            return Duration.ofHours(4);
        }

        return Duration.ofHours(12);
    }
}