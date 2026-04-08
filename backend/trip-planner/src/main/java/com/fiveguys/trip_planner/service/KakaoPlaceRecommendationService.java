package com.fiveguys.trip_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fiveguys.trip_planner.client.KakaoLocalClient;
import com.fiveguys.trip_planner.dto.ChatRequest;
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
    private final RecommendationIntentResolverService intentResolverService;
    private final RegionResolverService regionResolverService;
    private final RegionAliasResolverService regionAliasResolverService;
    private final RawAreaHintExtractorService rawAreaHintExtractorService;
    private final RecommendationCacheService recommendationCacheService;
    private final RecommendationCacheKeyGenerator cacheKeyGenerator;

    public KakaoPlaceRecommendationService(KakaoLocalClient kakaoLocalClient,
                                           RecommendationIntentResolverService intentResolverService,
                                           RegionResolverService regionResolverService,
                                           RegionAliasResolverService regionAliasResolverService,
                                           RawAreaHintExtractorService rawAreaHintExtractorService,
                                           RecommendationCacheService recommendationCacheService,
                                           RecommendationCacheKeyGenerator cacheKeyGenerator) {
        this.kakaoLocalClient = kakaoLocalClient;
        this.intentResolverService = intentResolverService;
        this.regionResolverService = regionResolverService;
        this.regionAliasResolverService = regionAliasResolverService;
        this.rawAreaHintExtractorService = rawAreaHintExtractorService;
        this.recommendationCacheService = recommendationCacheService;
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    public ChatResponse recommend(ChatRequest request) {
        String message = request.getMessage();

        String intent = intentResolverService.resolve(message);
        RegionResolverService.ResolvedRegion resolvedRegion = regionResolverService.resolve(message);

        String destination = resolvedRegion.getCity();
        String district = resolvedRegion.getDistrict();
        String neighborhood = resolvedRegion.getNeighborhood();
        String detailArea = resolvedRegion.getDetailName();

        if (!StringUtils.hasText(destination) && StringUtils.hasText(district)) {
            destination = district;
        }

        RegionAliasResolverService.ResolvedAlias alias = regionAliasResolverService.resolve(message, destination);

        String aliasQueryHint = "";
        String aliasTargetName = "";
        String aliasTargetParent = "";

        if (alias != null) {
            if (!StringUtils.hasText(destination) && StringUtils.hasText(alias.getCity())) {
                destination = alias.getCity();
            }

            aliasQueryHint = alias.getQueryHint();
            aliasTargetName = alias.getTargetName();
            aliasTargetParent = alias.getTargetParent();

            if (!StringUtils.hasText(detailArea) && StringUtils.hasText(aliasTargetName)) {
                detailArea = aliasTargetName;
            }

            if (!StringUtils.hasText(district) && StringUtils.hasText(aliasTargetParent)) {
                district = aliasTargetParent;
            }
        }

        String rawAreaHint = rawAreaHintExtractorService.extract(message, destination);

        log.info("[REGION RESOLVED] city={}, district={}, neighborhood={}, detailArea={}, aliasHint={}, rawAreaHint={}",
                destination, district, neighborhood, detailArea, aliasQueryHint, rawAreaHint);

        validateRegionStrict(destination);

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
                aliasQueryHint,
                aliasTargetName,
                aliasTargetParent,
                rawAreaHint,
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
            throw new LlmCallException("장소 검색 결과가 없습니다: " + buildDisplayDestination(destination, detailArea, neighborhood, district, aliasQueryHint));
        }

        List<RecommendationItemResponse> items = filterScoreSortAndMap(
                collectedDocs,
                intent,
                destination,
                detailArea,
                neighborhood,
                district,
                aliasQueryHint,
                aliasTargetParent
        );

        if (items.isEmpty()) {
            throw new LlmCallException("추천 가능한 결과가 없습니다: " + buildDisplayDestination(destination, detailArea, neighborhood, district, aliasQueryHint));
        }

        ChatResponse response = new ChatResponse(
                message,
                intent,
                buildDisplayDestination(destination, detailArea, neighborhood, district, aliasQueryHint),
                null,
                new RecommendationContentResponse(
                        new ArrayList<>(),
                        items
                )
        );

        recommendationCacheService.put(cacheKey, response, resolveTtl(intent));
        return response;
    }

    private void validateRegionStrict(String destination) {
        if (!StringUtils.hasText(destination)) {
            throw new LlmCallException("지역은 반드시 앞에 포함해야 합니다. 예: 부산 중구 맛집 추천, 서울 성수동 카페 추천");
        }
    }

    private List<String> buildQueryCandidates(String intent,
                                              String destination,
                                              String detailArea,
                                              String neighborhood,
                                              String district,
                                              String aliasQueryHint,
                                              String aliasTargetName,
                                              String aliasTargetParent,
                                              String rawAreaHint,
                                              String message) {
        Set<String> candidates = new LinkedHashSet<>();
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
                                            String district,
                                            String aliasQueryHint,
                                            String aliasTargetName,
                                            String aliasTargetParent,
                                            String rawAreaHint) {
        List<String> bases = new ArrayList<>();

        if (StringUtils.hasText(aliasQueryHint)) {
            bases.add(destination + " " + aliasQueryHint);
        }

        if (StringUtils.hasText(aliasTargetName)) {
            bases.add(destination + " " + aliasTargetName);
        }

        if (StringUtils.hasText(aliasTargetParent) && StringUtils.hasText(aliasQueryHint)) {
            bases.add(destination + " " + aliasTargetParent + " " + aliasQueryHint);
        }

        if (StringUtils.hasText(detailArea)) {
            bases.add(destination + " " + detailArea);
        }

        if (StringUtils.hasText(neighborhood)) {
            bases.add(destination + " " + neighborhood);
        }

        if (StringUtils.hasText(district)) {
            bases.add(destination + " " + district);
        }

        if (StringUtils.hasText(rawAreaHint)) {
            bases.add(destination + " " + rawAreaHint);
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
                                                                   String destination,
                                                                   String detailArea,
                                                                   String neighborhood,
                                                                   String district,
                                                                   String aliasQueryHint,
                                                                   String aliasTargetParent) {
        List<ScoredPlace> scoredPlaces = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (JsonNode doc : docs) {
            if (!isAllowedCategory(doc, intent)) {
                continue;
            }

            if (!isLocationRelevant(doc, destination, detailArea, neighborhood, district, aliasQueryHint, aliasTargetParent)) {
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

            int score = score(doc, intent, detailArea, neighborhood, district, aliasQueryHint, aliasTargetParent);

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

    private boolean isLocationRelevant(JsonNode doc,
                                       String destination,
                                       String detailArea,
                                       String neighborhood,
                                       String district,
                                       String aliasQueryHint,
                                       String aliasTargetParent) {
        String name = clean(doc.path("place_name").asText());
        String roadAddress = clean(doc.path("road_address_name").asText());
        String addressName = clean(doc.path("address_name").asText());

        String merged = ((roadAddress == null ? "" : roadAddress) + " "
                + (addressName == null ? "" : addressName) + " "
                + (name == null ? "" : name)).toLowerCase();

        if (StringUtils.hasText(destination) && !merged.contains(destination.toLowerCase())) {
            return false;
        }

        if (StringUtils.hasText(detailArea) && merged.contains(detailArea.toLowerCase())) {
            return true;
        }

        if (StringUtils.hasText(neighborhood) && merged.contains(neighborhood.toLowerCase())) {
            return true;
        }

        if (StringUtils.hasText(district) && merged.contains(district.toLowerCase())) {
            return true;
        }

        if (StringUtils.hasText(aliasQueryHint) && merged.contains(aliasQueryHint.toLowerCase())) {
            return true;
        }

        if (StringUtils.hasText(aliasTargetParent) && merged.contains(aliasTargetParent.toLowerCase())) {
            return true;
        }

        return !StringUtils.hasText(detailArea)
                && !StringUtils.hasText(neighborhood)
                && !StringUtils.hasText(district)
                && !StringUtils.hasText(aliasQueryHint)
                && !StringUtils.hasText(aliasTargetParent);
    }

    private int score(JsonNode doc,
                      String intent,
                      String detailArea,
                      String neighborhood,
                      String district,
                      String aliasQueryHint,
                      String aliasTargetParent) {
        int score = 0;

        String name = clean(doc.path("place_name").asText());
        String mergedAddress = combinedAddress(doc);
        String category = clean(doc.path("category_name").asText());

        if (StringUtils.hasText(detailArea)) {
            if (containsKeyword(mergedAddress, detailArea)) score += 15;
            if (containsKeyword(name, detailArea)) score += 8;
        }

        if (StringUtils.hasText(neighborhood)) {
            if (containsKeyword(mergedAddress, neighborhood)) score += 12;
            if (containsKeyword(name, neighborhood)) score += 6;
        }

        if (StringUtils.hasText(district)) {
            if (containsKeyword(mergedAddress, district)) score += 10;
            if (containsKeyword(name, district)) score += 4;
        }

        if (StringUtils.hasText(aliasQueryHint)) {
            if (containsKeyword(mergedAddress, aliasQueryHint)) score += 14;
            if (containsKeyword(name, aliasQueryHint)) score += 8;
        }

        if (StringUtils.hasText(aliasTargetParent)) {
            if (containsKeyword(mergedAddress, aliasTargetParent)) score += 9;
            if (containsKeyword(name, aliasTargetParent)) score += 4;
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

    private String combinedAddress(JsonNode doc) {
        String roadAddress = clean(doc.path("road_address_name").asText());
        String addressName = clean(doc.path("address_name").asText());

        if (StringUtils.hasText(roadAddress) && StringUtils.hasText(addressName)) {
            return roadAddress + " | " + addressName;
        }

        if (StringUtils.hasText(roadAddress)) {
            return roadAddress;
        }

        return addressName;
    }

    private String buildDedupKey(String name, String address) {
        return (clean(name) + "|" + clean(address))
                .toLowerCase()
                .replaceAll("[\\s\\-_/()\\[\\],.]", "");
    }

    private String buildDisplayDestination(String destination,
                                           String detailArea,
                                           String neighborhood,
                                           String district,
                                           String aliasQueryHint) {
        if (StringUtils.hasText(aliasQueryHint)) {
            return destination + " " + aliasQueryHint;
        }

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