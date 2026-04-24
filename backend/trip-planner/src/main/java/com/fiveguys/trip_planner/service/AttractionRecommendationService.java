package com.fiveguys.trip_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
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
public class AttractionRecommendationService {

    private static final int MIN_FALLBACK_ITEMS = 3;

    private final RegionResolverService regionResolverService;
    private final RegionAliasResolverService regionAliasResolverService;
    private final RecommendationCacheService recommendationCacheService;
    private final RecommendationCacheKeyGenerator cacheKeyGenerator;
    private final RecommendationDisplayService recommendationDisplayService;
    private final RawAreaHintExtractorService rawAreaHintExtractorService;
    private final AttractionQueryBuilder attractionQueryBuilder;
    private final AttractionDocumentCollector attractionDocumentCollector;
    private final AttractionFilterService attractionFilterService;
    private final AttractionMapper attractionMapper;
    private final AttractionSubIntentResolver attractionSubIntentResolver;

    public AttractionRecommendationService(RegionResolverService regionResolverService,
                                           RegionAliasResolverService regionAliasResolverService,
                                           RecommendationCacheService recommendationCacheService,
                                           RecommendationCacheKeyGenerator cacheKeyGenerator,
                                           RecommendationDisplayService recommendationDisplayService,
                                           RawAreaHintExtractorService rawAreaHintExtractorService,
                                           AttractionQueryBuilder attractionQueryBuilder,
                                           AttractionDocumentCollector attractionDocumentCollector,
                                           AttractionFilterService attractionFilterService,
                                           AttractionMapper attractionMapper,
                                           AttractionSubIntentResolver attractionSubIntentResolver) {
        this.regionResolverService = regionResolverService;
        this.regionAliasResolverService = regionAliasResolverService;
        this.recommendationCacheService = recommendationCacheService;
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.recommendationDisplayService = recommendationDisplayService;
        this.rawAreaHintExtractorService = rawAreaHintExtractorService;
        this.attractionQueryBuilder = attractionQueryBuilder;
        this.attractionDocumentCollector = attractionDocumentCollector;
        this.attractionFilterService = attractionFilterService;
        this.attractionMapper = attractionMapper;
        this.attractionSubIntentResolver = attractionSubIntentResolver;
    }

    public ChatResponse recommend(ChatRequest request) {
        String message = request.getMessage();

        validateRegionStrict(message);

        AttractionSubIntent subIntent = attractionSubIntentResolver.resolve(message);

        RegionResolverService.ResolvedRegion resolvedRegion = regionResolverService.resolve(message);

        String destination = resolvedRegion.getCity();
        String district = resolvedRegion.getDistrict();
        String neighborhood = resolvedRegion.getNeighborhood();
        String detailArea = resolvedRegion.getDetailName();

        String queryDestination = AttractionTextHelper.resolveEffectiveDestination(destination, district);

        RegionAliasResolverService.ResolvedAlias alias =
                regionAliasResolverService.resolve(
                        message,
                        StringUtils.hasText(queryDestination) ? queryDestination : destination
                );

        if (alias != null) {
            if (StringUtils.hasText(alias.getCity())) {
                destination = AttractionTextHelper.normalizeDisplayArea(alias.getCity());
                queryDestination = AttractionTextHelper.normalizeDisplayArea(alias.getCity());
            }

            if (StringUtils.hasText(alias.getTargetParent())) {
                district = AttractionTextHelper.normalizeDisplayArea(alias.getTargetParent());
            }

            if (StringUtils.hasText(alias.getTargetName())) {
                neighborhood = AttractionTextHelper.normalizeDisplayArea(alias.getTargetName());
                detailArea = AttractionTextHelper.normalizeDisplayArea(alias.getTargetName());
            }
        }

        queryDestination = AttractionTextHelper.normalizeDisplayArea(queryDestination);
        district = AttractionTextHelper.normalizeDisplayArea(district);
        neighborhood = AttractionTextHelper.normalizeDisplayArea(neighborhood);
        detailArea = AttractionTextHelper.normalizeDisplayArea(detailArea);

        String rawAreaHint = rawAreaHintExtractorService.extract(message, queryDestination);
        rawAreaHint = AttractionTextHelper.normalizeDisplayArea(rawAreaHint);

        if (StringUtils.hasText(rawAreaHint)
                && !AttractionTextHelper.normalizeAreaName(rawAreaHint).equals(AttractionTextHelper.normalizeAreaName(queryDestination))
                && !AttractionTextHelper.normalizeAreaName(rawAreaHint).equals(AttractionTextHelper.normalizeAreaName(detailArea))
                && !AttractionTextHelper.normalizeAreaName(rawAreaHint).equals(AttractionTextHelper.normalizeAreaName(neighborhood))
                && !AttractionTextHelper.normalizeAreaName(rawAreaHint).equals(AttractionTextHelper.normalizeAreaName(district))) {
            detailArea = rawAreaHint;
        }

        if (!StringUtils.hasText(queryDestination)) {
            throw new LlmCallException("지역명을 해석하지 못했습니다.");
        }

        String cacheKey = cacheKeyGenerator.generateAttractionKey(
                queryDestination,
                detailArea,
                neighborhood,
                district,
                message
        );

        ChatResponse cached = recommendationCacheService.get(cacheKey);

        if (cached != null) {
            return cached;
        }

        List<String> queries = attractionQueryBuilder.buildQueries(
                queryDestination,
                detailArea,
                neighborhood,
                district,
                subIntent
        );

        List<JsonNode> collectedDocs = attractionDocumentCollector.collectDocuments(queries);

        List<RecommendationItemResponse> items = attractionMapper.pickTopAttractions(
                collectedDocs,
                queryDestination,
                detailArea,
                neighborhood,
                district,
                subIntent
        );

        if (items.size() < MIN_FALLBACK_ITEMS) {
            List<String> fallbackQueries = attractionQueryBuilder.buildRelaxedFallbackQueries(
                    queryDestination,
                    district,
                    subIntent
            );

            List<JsonNode> fallbackDocs = attractionDocumentCollector.collectDocuments(fallbackQueries);

            collectedDocs.addAll(fallbackDocs);

            items = attractionMapper.pickTopAttractions(
                    collectedDocs,
                    queryDestination,
                    detailArea,
                    neighborhood,
                    district,
                    subIntent
            );
        }

        if (collectedDocs.isEmpty()) {
            throw new LlmCallException(
                    "명소 검색 결과가 없습니다: "
                            + buildDisplayDestination(queryDestination, detailArea, neighborhood, district)
            );
        }

        if (items.isEmpty()) {
            throw new LlmCallException(
                    "추천 가능한 명소가 없습니다: "
                            + buildDisplayDestination(queryDestination, detailArea, neighborhood, district)
            );
        }

        String displayDestination = buildDisplayDestination(
                queryDestination,
                detailArea,
                neighborhood,
                district
        );

        RecommendationDisplayService.DisplayMeta displayMeta =
                recommendationDisplayService.buildAttractionDisplayMeta(
                        message,
                        displayDestination
                );

        ChatResponse response = new ChatResponse(
                message,
                "ATTRACTION_RECOMMENDATION",
                displayDestination,
                null,
                new RecommendationContentResponse(
                        new ArrayList<>(),
                        items,
                        displayMeta.displayType(),
                        displayMeta.displayTitle()
                )
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

    private String buildDisplayDestination(String destination,
                                           String detailArea,
                                           String neighborhood,
                                           String district) {
        if (StringUtils.hasText(detailArea)) {
            return AttractionTextHelper.joinDistinctLocation(destination, detailArea);
        }

        if (StringUtils.hasText(neighborhood)) {
            return AttractionTextHelper.joinDistinctLocation(destination, neighborhood);
        }

        if (StringUtils.hasText(district) && !AttractionTextHelper.isCityOrCounty(district)) {
            return AttractionTextHelper.joinDistinctLocation(destination, district);
        }

        return destination;
    }
}