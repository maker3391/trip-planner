package com.fiveguys.trip_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fiveguys.trip_planner.client.KakaoLocalClient;
import com.fiveguys.trip_planner.dto.ChatRequest;
import com.fiveguys.trip_planner.exception.LlmCallException;
import com.fiveguys.trip_planner.response.ChatResponse;
import com.fiveguys.trip_planner.response.RecommendationContentResponse;
import com.fiveguys.trip_planner.response.RecommendationItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AttractionRecommendationService {

    private static final int MAX_COLLECT_SIZE = 20;
    private static final int MAX_ATTRACTION_ITEMS = 4;

    private final KakaoLocalClient kakaoLocalClient;
    private final RegionResolverService regionResolverService;
    private final RegionAliasResolverService regionAliasResolverService;
    private final RecommendationCacheService recommendationCacheService;
    private final RecommendationCacheKeyGenerator cacheKeyGenerator;

    public AttractionRecommendationService(KakaoLocalClient kakaoLocalClient,
                                           RegionResolverService regionResolverService,
                                           RegionAliasResolverService regionAliasResolverService,
                                           RecommendationCacheService recommendationCacheService,
                                           RecommendationCacheKeyGenerator cacheKeyGenerator) {
        this.kakaoLocalClient = kakaoLocalClient;
        this.regionResolverService = regionResolverService;
        this.regionAliasResolverService = regionAliasResolverService;
        this.recommendationCacheService = recommendationCacheService;
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    public ChatResponse recommend(ChatRequest request) {
        String message = request.getMessage();

        validateRegionStrict(message);

        RegionResolverService.ResolvedRegion resolvedRegion = regionResolverService.resolve(message);

        String destination = resolvedRegion.getCity();
        String district = resolvedRegion.getDistrict();
        String neighborhood = resolvedRegion.getNeighborhood();
        String detailArea = resolvedRegion.getDetailName();

        String queryDestination = resolveEffectiveDestination(destination, district);

        RegionAliasResolverService.ResolvedAlias alias =
                regionAliasResolverService.resolve(message, StringUtils.hasText(queryDestination) ? queryDestination : destination);

        if (alias != null) {
            if (StringUtils.hasText(alias.getCity())) {
                destination = normalizeDisplayArea(alias.getCity());
                queryDestination = normalizeDisplayArea(alias.getCity());
            }

            if (StringUtils.hasText(alias.getTargetParent())) {
                district = normalizeDisplayArea(alias.getTargetParent());
            }

            if (StringUtils.hasText(alias.getTargetName())) {
                neighborhood = normalizeDisplayArea(alias.getTargetName());
                detailArea = normalizeDisplayArea(alias.getTargetName());
            }
        }

        queryDestination = normalizeDisplayArea(queryDestination);
        destination = normalizeDisplayArea(destination);
        district = normalizeDisplayArea(district);
        neighborhood = normalizeDisplayArea(neighborhood);
        detailArea = normalizeDisplayArea(detailArea);

        if (!StringUtils.hasText(queryDestination)) {
            throw new LlmCallException("지역명을 해석하지 못했습니다.");
        }

        String cacheKey = cacheKeyGenerator.generateAttractionKey(queryDestination, detailArea, neighborhood, district);
        ChatResponse cached = recommendationCacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<String> queries = buildQueries(queryDestination, detailArea, neighborhood, district);
        List<JsonNode> collectedDocs = new ArrayList<>();

        for (String query : queries) {
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
            throw new LlmCallException("명소 검색 결과가 없습니다: " + buildDisplayDestination(queryDestination, detailArea, neighborhood, district));
        }

        List<RecommendationItemResponse> items = pickTopAttractions(
                collectedDocs,
                queryDestination,
                detailArea,
                neighborhood,
                district
        );

        if (items.isEmpty()) {
            throw new LlmCallException("추천 가능한 명소가 없습니다: " + buildDisplayDestination(queryDestination, detailArea, neighborhood, district));
        }

        ChatResponse response = new ChatResponse(
                message,
                "ATTRACTION_RECOMMENDATION",
                buildDisplayDestination(queryDestination, detailArea, neighborhood, district),
                null,
                new RecommendationContentResponse(new ArrayList<>(), items)
        );

        recommendationCacheService.put(cacheKey, response, Duration.ofHours(6));
        return response;
    }

    private void validateRegionStrict(String message) {
        if (!regionResolverService.hasExplicitTopLevelArea(message)) {
            throw new LlmCallException(
                    "지역명이 모호합니다. 예: 부산 명소 추천, 경주 대표 관광지 추천, 제주 가볼만한 곳 추천"
            );
        }
    }

    private String resolveEffectiveDestination(String destination, String district) {
        String districtHead = extractCityHead(district);

        if (isCityOrCounty(districtHead)) {
            return districtHead;
        }

        if (isCityOrCounty(district)) {
            return district;
        }

        return destination;
    }

    private String extractCityHead(String value) {
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

    private boolean isCityOrCounty(String value) {
        return StringUtils.hasText(value)
                && (value.endsWith("시") || value.endsWith("군"));
    }

    private List<String> buildQueries(String destination,
                                      String detailArea,
                                      String neighborhood,
                                      String district) {
        Set<String> result = new LinkedHashSet<>();

        List<String> bases = new ArrayList<>();
        if (StringUtils.hasText(detailArea)) {
            bases.add(joinDistinctLocation(destination, detailArea));
        }
        if (StringUtils.hasText(neighborhood)) {
            bases.add(joinDistinctLocation(destination, neighborhood));
        }
        if (StringUtils.hasText(district) && !isCityOrCounty(district)) {
            bases.add(joinDistinctLocation(destination, district));
        }
        bases.add(destination);

        for (String base : bases) {
            result.add(base + " 명소");
            result.add(base + " 관광지");
            result.add(base + " 대표 관광지");
            result.add(base + " 가볼만한곳");
        }

        return new ArrayList<>(result);
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

    private List<RecommendationItemResponse> pickTopAttractions(List<JsonNode> docs,
                                                                String destination,
                                                                String detailArea,
                                                                String neighborhood,
                                                                String district) {
        List<ScoredPlace> scoredPlaces = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (JsonNode doc : docs) {
            if (!isAllowedAttraction(doc)) {
                continue;
            }

            if (!isLocationRelevant(doc, destination, detailArea, neighborhood, district)) {
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

            int score = score(doc, detailArea, neighborhood, district);
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

            if (result.size() >= MAX_ATTRACTION_ITEMS) {
                break;
            }
        }

        return result;
    }

    private boolean isAllowedAttraction(JsonNode doc) {
        String categoryGroupCode = clean(doc.path("category_group_code").asText());
        String categoryName = clean(doc.path("category_name").asText());

        if ("AT4".equals(categoryGroupCode)) {
            return true;
        }

        return containsKeyword(categoryName,
                "관광명소", "문화시설", "유적", "박물관", "미술관", "공원",
                "전망대", "폭포", "해변", "산", "사찰", "궁", "랜드마크", "케이블카");
    }

    private boolean isLocationRelevant(JsonNode doc,
                                       String destination,
                                       String detailArea,
                                       String neighborhood,
                                       String district) {
        String name = clean(doc.path("place_name").asText());
        String roadAddress = clean(doc.path("road_address_name").asText());
        String addressName = clean(doc.path("address_name").asText());

        String merged = ((roadAddress == null ? "" : roadAddress) + " "
                + (addressName == null ? "" : addressName) + " "
                + (name == null ? "" : name)).toLowerCase();

        boolean destinationMatch = !StringUtils.hasText(destination) || containsLooseRegion(merged, destination);
        boolean detailMatch = !StringUtils.hasText(detailArea) || containsLooseRegion(merged, detailArea);
        boolean neighborhoodMatch = !StringUtils.hasText(neighborhood) || containsLooseRegion(merged, neighborhood);
        boolean districtMatch = !StringUtils.hasText(district) || isCityOrCounty(district) || containsLooseRegion(merged, district);

        if (!destinationMatch) {
            return false;
        }

        if (StringUtils.hasText(detailArea)) {
            return detailMatch;
        }

        if (StringUtils.hasText(neighborhood)) {
            return neighborhoodMatch;
        }

        if (StringUtils.hasText(district) && !isCityOrCounty(district)) {
            return districtMatch;
        }

        return true;
    }

    private int score(JsonNode doc,
                      String detailArea,
                      String neighborhood,
                      String district) {
        int score = 0;

        String name = clean(doc.path("place_name").asText());
        String mergedAddress = combinedAddress(doc);
        String category = clean(doc.path("category_name").asText());
        String categoryGroupCode = clean(doc.path("category_group_code").asText());

        if ("AT4".equals(categoryGroupCode)) {
            score += 20;
        }

        if (StringUtils.hasText(detailArea)) {
            if (containsLooseRegion(mergedAddress, detailArea)) score += 18;
            if (containsLooseRegion(name, detailArea)) score += 10;
        }

        if (StringUtils.hasText(neighborhood)) {
            if (containsLooseRegion(mergedAddress, neighborhood)) score += 15;
            if (containsLooseRegion(name, neighborhood)) score += 8;
        }

        if (StringUtils.hasText(district) && !isCityOrCounty(district)) {
            if (containsLooseRegion(mergedAddress, district)) score += 10;
        }

        if (containsKeyword(category, "관광명소", "유적", "박물관", "미술관", "공원", "전망대", "해변")) {
            score += 6;
        }

        if (StringUtils.hasText(doc.path("road_address_name").asText())) {
            score += 2;
        }

        if (StringUtils.hasText(doc.path("phone").asText())) {
            score += 1;
        }

        return score;
    }

    private boolean looksLikeNoise(String name, String category) {
        if (!StringUtils.hasText(name)) {
            return true;
        }

        if (name.length() > 40) {
            return true;
        }

        if (containsKeyword(category, "숙박", "음식점", "카페", "주점")) {
            return true;
        }

        return containsKeyword(name,
                "호텔", "모텔", "펜션", "게스트하우스", "리조트",
                "맛집", "카페", "주차장", "관리사무소", "행정복지센터", "주민센터");
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
                                           String district) {
        if (StringUtils.hasText(detailArea)) {
            return joinDistinctLocation(destination, detailArea);
        }

        if (StringUtils.hasText(neighborhood)) {
            return joinDistinctLocation(destination, neighborhood);
        }

        if (StringUtils.hasText(district) && !isCityOrCounty(district)) {
            return joinDistinctLocation(destination, district);
        }

        return destination;
    }

    private String joinDistinctLocation(String first, String second) {
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
        return value.replaceAll("\\s+", "").trim().toLowerCase();
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

    private boolean containsLooseRegion(String value, String keyword) {
        if (!StringUtils.hasText(value) || !StringUtils.hasText(keyword)) {
            return false;
        }

        String normalizedValue = normalizeForMatch(value);
        String normalizedKeyword = normalizeForMatch(keyword);

        if (normalizedValue.equals(normalizedKeyword)) {
            return true;
        }

        if (normalizedValue.contains(normalizedKeyword)) {
            return true;
        }

        String strippedKeyword = stripRegionSuffixForLooseMatch(normalizedKeyword);
        return StringUtils.hasText(strippedKeyword)
                && strippedKeyword.length() >= 2
                && normalizedValue.contains(strippedKeyword);
    }

    private String stripRegionSuffixForLooseMatch(String value) {
        return value.replaceAll("(특별자치도|특별자치시|광역시|특별시|도|시|군|구|동|읍|면|리)$", "").trim();
    }

    private String normalizeForMatch(String value) {
        return value.toLowerCase()
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String clean(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private record ScoredPlace(String name, String address, String placeUrl, String category, int score) {
    }
}