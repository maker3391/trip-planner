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
import java.util.List;

@Service
public class KakaoPlaceRecommendationService {

    private static final int MAX_ITEMS = 5;

    private static final String STAY_SUBTYPE_GENERIC = "generic";
    private static final String STAY_SUBTYPE_HOTEL = "hotel";
    private static final String STAY_SUBTYPE_MOTEL = "motel";
    private static final String STAY_SUBTYPE_PENSION = "pension";
    private static final String STAY_SUBTYPE_RESORT = "resort";
    private static final String STAY_SUBTYPE_GUEST_HOUSE = "guesthouse";
    private static final String STAY_SUBTYPE_HANOK = "hanok";
    private static final String STAY_SUBTYPE_POOL_VILLA = "poolvilla";

    private final KakaoLocalClient kakaoLocalClient;
    private final RecommendationIntentResolverService intentResolverService;
    private final RegionResolverService regionResolverService;
    private final RegionAliasResolverService regionAliasResolverService;
    private final RawAreaHintExtractorService rawAreaHintExtractorService;
    private final RecommendationCacheService recommendationCacheService;
    private final RecommendationCacheKeyGenerator cacheKeyGenerator;
    private final KakaoPlaceQueryBuilder kakaoPlaceQueryBuilder;
    private final KakaoPlaceMapper kakaoPlaceMapper;
    private final KakaoPlaceScoringService kakaoPlaceScoringService;
    private final RecommendationDisplayService recommendationDisplayService;

    public KakaoPlaceRecommendationService(KakaoLocalClient kakaoLocalClient,
                                           RecommendationIntentResolverService intentResolverService,
                                           RegionResolverService regionResolverService,
                                           RegionAliasResolverService regionAliasResolverService,
                                           RawAreaHintExtractorService rawAreaHintExtractorService,
                                           RecommendationCacheService recommendationCacheService,
                                           RecommendationCacheKeyGenerator cacheKeyGenerator,
                                           KakaoPlaceQueryBuilder kakaoPlaceQueryBuilder,
                                           KakaoPlaceMapper kakaoPlaceMapper,
                                           KakaoPlaceScoringService kakaoPlaceScoringService, RecommendationDisplayService recommendationDisplayService) {
        this.kakaoLocalClient = kakaoLocalClient;
        this.intentResolverService = intentResolverService;
        this.regionResolverService = regionResolverService;
        this.regionAliasResolverService = regionAliasResolverService;
        this.rawAreaHintExtractorService = rawAreaHintExtractorService;
        this.recommendationCacheService = recommendationCacheService;
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.kakaoPlaceQueryBuilder = kakaoPlaceQueryBuilder;
        this.kakaoPlaceMapper = kakaoPlaceMapper;
        this.kakaoPlaceScoringService = kakaoPlaceScoringService;
        this.recommendationDisplayService = recommendationDisplayService;
    }

    public ChatResponse recommend(ChatRequest request) {
        String message = request.getMessage();

        validateRegionStrict(message);

        String intent = intentResolverService.resolve(message);
        String staySubtype = resolveStaySubtype(message, intent);

        RegionResolverService.ResolvedRegion resolvedRegion = regionResolverService.resolve(message);

        String province = resolvedRegion.getProvince();
        String destination = resolvedRegion.getCity();
        String district = resolvedRegion.getDistrict();
        String neighborhood = resolvedRegion.getNeighborhood();
        String detailArea = resolvedRegion.getDetailName();

        String queryDestination = resolveEffectiveDestination(destination, district);

        boolean skipAlias = shouldSkipAliasForExplicitGwangju(
                message,
                province,
                destination,
                district,
                neighborhood,
                detailArea
        );

        RegionAliasResolverService.ResolvedAlias alias = skipAlias
                ? null
                : regionAliasResolverService.resolve(
                message,
                StringUtils.hasText(queryDestination) ? queryDestination : destination
        );

        String aliasQueryHint = "";
        String aliasTargetName = "";
        String aliasTargetParent = "";

        if (alias != null) {
            String normalizedAliasCity = normalizeDisplayArea(alias.getCity());
            String normalizedAliasHint = normalizeDisplayArea(alias.getQueryHint());
            String normalizedAliasTargetName = normalizeDisplayArea(alias.getTargetName());
            String normalizedAliasTargetParent = normalizeDisplayArea(alias.getTargetParent());

            boolean hasResolvedSpecificArea =
                    StringUtils.hasText(detailArea) || StringUtils.hasText(district) || StringUtils.hasText(neighborhood);

            boolean aliasIsMoreSpecific =
                    StringUtils.hasText(normalizedAliasTargetName)
                            && !normalizeAreaName(normalizedAliasTargetName).equals(normalizeAreaName(normalizedAliasCity));

            if ((!hasResolvedSpecificArea) || aliasIsMoreSpecific) {
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
        province = normalizeDisplayArea(province);
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

        List<String> queryCandidates = kakaoPlaceQueryBuilder.buildQueryCandidates(
                intent,
                staySubtype,
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
        }

        if (collectedDocs.isEmpty()) {
            throw new LlmCallException("장소 검색 결과가 없습니다: "
                    + buildDisplayDestination(queryDestination, detailArea, neighborhood, district, aliasQueryHint));
        }

        List<KakaoPlaceScoringService.RecommendationCandidate> ranked = kakaoPlaceScoringService.filterAndRank(
                collectedDocs,
                intent,
                staySubtype,
                province,
                queryDestination,
                detailArea,
                neighborhood,
                district,
                aliasQueryHint,
                aliasTargetParent,
                message
        );

        if (ranked.isEmpty()) {
            throw new LlmCallException("추천 가능한 결과가 없습니다: "
                    + buildDisplayDestination(queryDestination, detailArea, neighborhood, district, aliasQueryHint));
        }

        List<RecommendationItemResponse> items = new ArrayList<>();
        for (KakaoPlaceScoringService.RecommendationCandidate candidate : ranked) {
            if (items.size() >= MAX_ITEMS) {
                break;
            }
            items.add(kakaoPlaceMapper.toRecommendationItemResponse(intent, candidate.doc()));
        }

        String displayDestination = buildDisplayDestination(
                queryDestination,
                detailArea,
                neighborhood,
                district,
                aliasQueryHint
        );

        RecommendationDisplayService.DisplayMeta displayMeta =
                recommendationDisplayService.buildPlaceDisplayMeta(
                        intent,
                        message,
                        displayDestination,
                        items
                );

        ChatResponse response = new ChatResponse(
                message,
                intent,
                displayDestination,
                null,
                new RecommendationContentResponse(
                        new ArrayList<>(),
                        items,
                        displayMeta.displayType(),
                        displayMeta.displayTitle()
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

    private String resolveStaySubtype(String message, String intent) {
        if (!"STAY_RECOMMENDATION".equals(intent)) {
            return STAY_SUBTYPE_GENERIC;
        }

        String normalizedMessage = message == null ? "" : message.toLowerCase();

        if (normalizedMessage.contains("풀빌라")) {
            return STAY_SUBTYPE_POOL_VILLA;
        }
        if (normalizedMessage.contains("한옥스테이")) {
            return STAY_SUBTYPE_HANOK;
        }
        if (normalizedMessage.contains("게스트하우스")) {
            return STAY_SUBTYPE_GUEST_HOUSE;
        }
        if (normalizedMessage.contains("리조트")) {
            return STAY_SUBTYPE_RESORT;
        }
        if (normalizedMessage.contains("펜션")) {
            return STAY_SUBTYPE_PENSION;
        }
        if (normalizedMessage.contains("무인텔") || normalizedMessage.contains("모텔")) {
            return STAY_SUBTYPE_MOTEL;
        }
        if (normalizedMessage.contains("호텔")) {
            return STAY_SUBTYPE_HOTEL;
        }

        return STAY_SUBTYPE_GENERIC;
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

    private boolean shouldSkipAliasForExplicitGwangju(String message,
                                                      String province,
                                                      String destination,
                                                      String district,
                                                      String neighborhood,
                                                      String detailArea) {
        String normalizedMessage = normalizeAreaName(message);
        String normalizedProvince = normalizeAreaName(province);
        String normalizedDestination = normalizeAreaName(destination);
        String normalizedDistrict = normalizeAreaName(district);
        String normalizedNeighborhood = normalizeAreaName(neighborhood);
        String normalizedDetailArea = normalizeAreaName(detailArea);

        if ("광주".equals(normalizedProvince) || normalizedMessage.contains("광주광역시")) {
            return true;
        }

        if ("경기".equals(normalizedProvince)
                && ("광주시".equals(normalizedDestination)
                || "광주시".equals(normalizedDistrict)
                || "광주시".equals(normalizedDetailArea)
                || "광주시".equals(normalizedNeighborhood))) {
            return true;
        }

        if (normalizedMessage.contains("경기도광주")
                || normalizedMessage.contains("경기광주")
                || normalizedMessage.contains("경기도광주시")
                || normalizedMessage.contains("경기광주시")) {
            return true;
        }

        if (normalizedMessage.startsWith("광주")
                || normalizedMessage.contains("광주맛집")
                || normalizedMessage.contains("광주숙소")
                || normalizedMessage.contains("광주식당")
                || normalizedMessage.contains("광주밥집")) {
            return true;
        }

        return false;
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
}