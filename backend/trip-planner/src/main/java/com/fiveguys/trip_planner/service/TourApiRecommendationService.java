package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.client.TourApiClient;
import com.fiveguys.trip_planner.dto.ChatRequest;
import com.fiveguys.trip_planner.dto.RegionCodeInfo;
import com.fiveguys.trip_planner.dto.TourApiPlaceCandidate;
import com.fiveguys.trip_planner.exception.LlmCallException;
import com.fiveguys.trip_planner.response.ChatResponse;
import com.fiveguys.trip_planner.response.RecommendationContentResponse;
import com.fiveguys.trip_planner.response.RecommendationItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TourApiRecommendationService {

    private static final int FETCH_SIZE = 20;
    private static final int MAX_ITEMS = 5;

    private final RecommendationIntentResolverService intentResolverService;
    private final RecommendationCacheService recommendationCacheService;
    private final RecommendationCacheKeyGenerator cacheKeyGenerator;
    private final DetailAreaParsingService detailAreaParsingService;
    private final RegionExtractionService regionExtractionService;
    private final RegionCodeMappingService regionCodeMappingService;
    private final TourApiClient tourApiClient;

    public TourApiRecommendationService(RecommendationIntentResolverService intentResolverService,
                                        RecommendationCacheService recommendationCacheService,
                                        RecommendationCacheKeyGenerator cacheKeyGenerator,
                                        DetailAreaParsingService detailAreaParsingService,
                                        RegionExtractionService regionExtractionService,
                                        RegionCodeMappingService regionCodeMappingService,
                                        TourApiClient tourApiClient) {
        this.intentResolverService = intentResolverService;
        this.recommendationCacheService = recommendationCacheService;
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.detailAreaParsingService = detailAreaParsingService;
        this.regionExtractionService = regionExtractionService;
        this.regionCodeMappingService = regionCodeMappingService;
        this.tourApiClient = tourApiClient;
    }

    public ChatResponse recommend(ChatRequest request) {
        String cacheKey = cacheKeyGenerator.generate(request.getMessage());

        ChatResponse cached = recommendationCacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String intent = intentResolverService.resolve(request.getMessage());
        String detailArea = detailAreaParsingService.extractDetailArea(request.getMessage());
        String destination = regionExtractionService.extract(request.getMessage());

        if (!StringUtils.hasText(destination) && StringUtils.hasText(detailArea)) {
            destination = detailAreaParsingService.resolveParentCity(detailArea);
        }

        if (!StringUtils.hasText(destination)) {
            throw new LlmCallException("지역 추출 실패: " + request.getMessage());
        }

        RegionCodeInfo regionCodeInfo = regionCodeMappingService.resolve(destination, detailArea);

        if (regionCodeInfo == null && StringUtils.hasText(detailArea)) {
            regionCodeInfo = regionCodeMappingService.resolve(destination, null);
        }

        if (regionCodeInfo == null) {
            throw new LlmCallException("지역 코드 매핑 실패: " + destination);
        }

        List<TourApiPlaceCandidate> rawCandidates = "STAY_RECOMMENDATION".equals(intent)
                ? tourApiClient.fetchStays(regionCodeInfo.getAreaCode(), regionCodeInfo.getSigunguCode(), FETCH_SIZE)
                : tourApiClient.fetchRestaurants(regionCodeInfo.getAreaCode(), regionCodeInfo.getSigunguCode(), FETCH_SIZE);

        List<RecommendationItemResponse> items = rankAndMap(rawCandidates, detailArea);

        ChatResponse response = new ChatResponse(
                request.getMessage(),
                intent,
                buildDisplayDestination(regionCodeInfo.getDestination(), detailArea),
                null,
                new RecommendationContentResponse(
                        new ArrayList<>(),
                        items
                )
        );

        recommendationCacheService.put(cacheKey, response);
        return response;
    }

    private List<RecommendationItemResponse> rankAndMap(List<TourApiPlaceCandidate> candidates, String detailArea) {
        List<TourApiPlaceCandidate> sorted = new ArrayList<>(candidates);
        sorted.sort(Comparator.comparingInt((TourApiPlaceCandidate candidate) -> score(candidate, detailArea)).reversed());

        List<RecommendationItemResponse> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (TourApiPlaceCandidate candidate : sorted) {
            String title = cleanTitle(candidate.getTitle());
            if (!StringUtils.hasText(title)) {
                continue;
            }

            String signature = buildSignature(title);
            if (seen.contains(signature)) {
                continue;
            }

            seen.add(signature);
            result.add(new RecommendationItemResponse(title));

            if (result.size() >= MAX_ITEMS) {
                break;
            }
        }

        return result;
    }

    private int score(TourApiPlaceCandidate candidate, String detailArea) {
        int score = 0;

        if (StringUtils.hasText(candidate.getFirstImage())) {
            score += 2;
        }

        if (StringUtils.hasText(candidate.getAddr1())) {
            score += 1;
        }

        if (detailAreaParsingService.matchesNearby(detailArea, candidate.getTitle())) {
            score += 5;
        }

        if (detailAreaParsingService.matchesNearby(detailArea, candidate.getAddr1())) {
            score += 4;
        }

        if (detailAreaParsingService.matchesNearby(detailArea, candidate.getAddr2())) {
            score += 3;
        }

        return score;
    }

    private String cleanTitle(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String cleaned = value.trim()
                .replaceAll("<[^>]*>", "")
                .replaceAll("\\s+", " ")
                .trim();

        if (!StringUtils.hasText(cleaned)) {
            return null;
        }

        if (cleaned.length() > 40) {
            return null;
        }

        return cleaned;
    }

    private String buildSignature(String value) {
        return value.toLowerCase()
                .replaceAll("[\\s\\-_/()\\[\\],.]", "");
    }

    private String buildDisplayDestination(String destination, String detailArea) {
        if (!StringUtils.hasText(detailArea)) {
            return destination;
        }
        return destination + " " + detailArea;
    }
}