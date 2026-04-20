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
public class KakaoPlaceRecommendationService {

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

        validateRegionStrict(message);

        String intent = intentResolverService.resolve(message);
        RegionResolverService.ResolvedRegion resolvedRegion = regionResolverService.resolve(message);

        String destination = resolvedRegion.getCity();
        String district = resolvedRegion.getDistrict();
        String neighborhood = resolvedRegion.getNeighborhood();
        String detailArea = resolvedRegion.getDetailName();

        String queryDestination = resolveEffectiveDestination(destination, district);

        RegionAliasResolverService.ResolvedAlias alias =
                regionAliasResolverService.resolve(message, StringUtils.hasText(queryDestination) ? queryDestination : destination);

        String aliasQueryHint = "";
        String aliasTargetName = "";
        String aliasTargetParent = "";

        if (alias != null) {
            String normalizedAliasCity = normalizeDisplayArea(alias.getCity());
            String normalizedAliasHint = normalizeDisplayArea(alias.getQueryHint());
            String normalizedAliasTargetName = normalizeDisplayArea(alias.getTargetName());
            String normalizedAliasTargetParent = normalizeDisplayArea(alias.getTargetParent());

            boolean currentAlreadySpecific =
                    StringUtils.hasText(detailArea)
                            && !normalizeAreaName(detailArea).equals(normalizeAreaName(destination));

            boolean aliasIsMoreSpecific =
                    StringUtils.hasText(normalizedAliasTargetName)
                            && !normalizeAreaName(normalizedAliasTargetName).equals(normalizeAreaName(normalizedAliasCity));

            if (!currentAlreadySpecific || aliasIsMoreSpecific) {
                if (StringUtils.hasText(normalizedAliasCity)) {
                    destination = normalizedAliasCity;
                    queryDestination = normalizedAliasCity;
                }

                aliasQueryHint = normalizedAliasHint;
                aliasTargetName = normalizedAliasTargetName;
                aliasTargetParent = normalizedAliasTargetParent;

                if (StringUtils.hasText(aliasTargetParent)) {
                    district = aliasTargetParent;
                }

                if (StringUtils.hasText(aliasTargetName)) {
                    neighborhood = aliasTargetName;
                    detailArea = aliasTargetName;
                }
            }
        }

        if (!StringUtils.hasText(queryDestination)) {
            queryDestination = destination;
        }

        queryDestination = normalizeDisplayArea(queryDestination);
        destination = normalizeDisplayArea(destination);
        district = normalizeDisplayArea(district);
        neighborhood = normalizeDisplayArea(neighborhood);
        detailArea = normalizeDisplayArea(detailArea);

        validateResolvedDestination(queryDestination);

        String rawAreaHint = rawAreaHintExtractorService.extract(message, queryDestination);
        rawAreaHint = normalizeDisplayArea(rawAreaHint);

        String cacheKey = cacheKeyGenerator.generatePlaceKey(
                intent,
                queryDestination,
                detailArea,
                neighborhood,
                district,
                aliasQueryHint,
                rawAreaHint,
                message
        );

        ChatResponse cached = recommendationCacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<String> queryCandidates = buildQueryCandidates(
                intent,
                queryDestination,
                detailArea,
                neighborhood,
                district,
                aliasQueryHint,
                aliasTargetName,
                aliasTargetParent,
                rawAreaHint,
                message
        );

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
            throw new LlmCallException("장소 검색 결과가 없습니다: "
                    + buildDisplayDestination(queryDestination, detailArea, neighborhood, district, aliasQueryHint));
        }

        List<RecommendationItemResponse> items = filterScoreSortAndMap(
                collectedDocs,
                intent,
                queryDestination,
                detailArea,
                neighborhood,
                district,
                aliasQueryHint,
                aliasTargetParent
        );

        if (items.isEmpty()) {
            throw new LlmCallException("추천 가능한 결과가 없습니다: "
                    + buildDisplayDestination(queryDestination, detailArea, neighborhood, district, aliasQueryHint));
        }

        ChatResponse response = new ChatResponse(
                message,
                intent,
                buildDisplayDestination(queryDestination, detailArea, neighborhood, district, aliasQueryHint),
                null,
                new RecommendationContentResponse(
                        new ArrayList<>(),
                        items
                )
        );

        recommendationCacheService.put(cacheKey, response, resolveTtl(intent));
        return response;
    }

    private void validateRegionStrict(String message) {
        if (!regionResolverService.hasExplicitTopLevelArea(message)) {
            throw new LlmCallException(
                    "지역명이 모호합니다. '시/군/구'를 포함하여 다시 입력해 주세요. (예: 서울 강남구 숙소 추천, 부산 해운대구 맛집 추천, 제주 2박 3일 일정 추천)"
            );
        }
    }

    private void validateResolvedDestination(String destination) {
        if (!StringUtils.hasText(destination)) {
            throw new LlmCallException(
                    "지역명이 모호합니다. '시/군/구'를 포함하여 다시 입력해 주세요. (예: 서울 강남구 숙소 추천, 부산 해운대구 맛집 추천, 제주 2박 3일 일정 추천)"
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

        List<String> keywords = buildIntentKeywords(intent, message);

        for (String base : locationBases) {
            for (String keyword : keywords) {
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
        result.add(value);

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
                result.add("강원도");
                result.add("강원특별자치도");
                break;
            case "제주":
                result.add("제주도");
                result.add("제주특별자치도");
                break;
            case "경기":
                result.add("경기도");
                break;
            case "서울":
                result.add("서울특별시");
                break;
            case "부산":
                result.add("부산광역시");
                break;
            case "대구":
                result.add("대구광역시");
                break;
            case "인천":
                result.add("인천광역시");
                break;
            case "광주":
                result.add("광주광역시");
                break;
            case "대전":
                result.add("대전광역시");
                break;
            case "울산":
                result.add("울산광역시");
                break;
            case "세종":
                result.add("세종특별자치시");
                break;
            default:
                break;
        }

        return new ArrayList<>(result);
    }

    private List<String> buildIntentKeywords(String intent, String message) {
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        String normalizedMessage = message == null ? "" : message.toLowerCase();

        if ("RESTAURANT_RECOMMENDATION".equals(intent)) {
            keywords.add("맛집");
            keywords.add("식당");
            keywords.add("밥집");
            keywords.add("한식");

            if (normalizedMessage.contains("카페")) {
                keywords.add("카페");
            }
            if (normalizedMessage.contains("술집") || normalizedMessage.contains("주점")) {
                keywords.add("술집");
                keywords.add("주점");
            }
        } else {
            keywords.add("숙소");
            keywords.add("호텔");
            keywords.add("모텔");
            keywords.add("펜션");
            keywords.add("게스트하우스");

            if (normalizedMessage.contains("리조트")) {
                keywords.add("리조트");
            }
            if (normalizedMessage.contains("풀빌라")) {
                keywords.add("풀빌라");
            }
            if (normalizedMessage.contains("한옥스테이")) {
                keywords.add("한옥스테이");
            }
        }

        return new ArrayList<>(keywords);
    }

    private List<JsonNode> extractDocuments(JsonNode root) {
        List<JsonNode> result = new ArrayList<>();

        if (root == null || !root.has("documents") || !root.get("documents").isArray()) {
            return result;
        }

        for (JsonNode doc : root.get("documents")) {
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
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<ScoredPlace> scored = new ArrayList<>();

        for (JsonNode doc : docs) {
            int score = scorePlace(doc, intent, destination, detailArea, neighborhood, district, aliasQueryHint, aliasTargetParent);
            if (score <= 0) {
                continue;
            }

            String name = text(doc, "place_name");
            String address = resolveAddress(doc);
            String key = (name + "|" + address).trim();

            if (!seen.add(key)) {
                continue;
            }

            scored.add(new ScoredPlace(doc, score));
        }

        scored.sort(Comparator.comparingInt(ScoredPlace::score).reversed());

        List<RecommendationItemResponse> result = new ArrayList<>();
        for (ScoredPlace place : scored) {
            if (result.size() >= MAX_ITEMS) {
                break;
            }

            JsonNode doc = place.doc();
            result.add(new RecommendationItemResponse(
                    text(doc, "place_name"),
                    resolveAddress(doc),
                    text(doc, "place_url"),
                    resolveCategory(intent, doc)
            ));
        }

        return result;
    }

    private int scorePlace(JsonNode doc,
                           String intent,
                           String destination,
                           String detailArea,
                           String neighborhood,
                           String district,
                           String aliasQueryHint,
                           String aliasTargetParent) {
        int score = 0;

        String placeName = normalizeAreaName(text(doc, "place_name"));
        String address = normalizeAreaName(resolveAddress(doc));
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
            if (category.contains("음식점")) score += 30;
            if (category.contains("한식")) score += 20;
            if (category.contains("중식")) score += 20;
            if (category.contains("일식")) score += 20;
            if (category.contains("양식")) score += 20;
            if (category.contains("카페")) score += 15;
            if (category.contains("주점")) score += 15;
        } else {
            if (category.contains("숙박")) score += 35;
            if (category.contains("호텔")) score += 25;
            if (category.contains("모텔")) score += 25;
            if (category.contains("펜션")) score += 25;
            if (category.contains("리조트")) score += 25;
            if (category.contains("게스트하우스")) score += 25;
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
        return address.contains(normalized) || placeName.contains(normalized);
    }

    private String resolveCategory(String intent, JsonNode doc) {
        String category = text(doc, "category_name");

        if ("RESTAURANT_RECOMMENDATION".equals(intent)) {
            if (category.contains("카페")) {
                return "카페";
            }
            if (category.contains("주점")) {
                return "술집";
            }
            return "맛집";
        }

        return "숙소";
    }

    private String resolveAddress(JsonNode doc) {
        String road = text(doc, "road_address_name");
        if (StringUtils.hasText(road)) {
            return road;
        }
        return text(doc, "address_name");
    }

    private String buildDisplayDestination(String destination,
                                           String detailArea,
                                           String neighborhood,
                                           String district,
                                           String aliasQueryHint) {
        if (StringUtils.hasText(detailArea)) {
            return detailArea;
        }
        if (StringUtils.hasText(neighborhood)) {
            return neighborhood;
        }
        if (StringUtils.hasText(aliasQueryHint)) {
            return aliasQueryHint;
        }
        if (StringUtils.hasText(district)) {
            return district;
        }
        return destination;
    }

    private Duration resolveTtl(String intent) {
        if ("STAY_RECOMMENDATION".equals(intent)) {
            return Duration.ofHours(12);
        }
        return Duration.ofHours(6);
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

    private record ScoredPlace(JsonNode doc, int score) {
    }
}