package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.client.TourApiClient;
import com.fiveguys.trip_planner.dto.RecommendedPlaceDto;
import com.fiveguys.trip_planner.dto.RegionTarget;
import com.fiveguys.trip_planner.dto.ResolvedRegion;
import com.fiveguys.trip_planner.exception.LlmCallException;
import com.fiveguys.trip_planner.response.DayCourseResponse;
import com.fiveguys.trip_planner.response.TripRecommendationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class TripRecommendationService {

    private static final int PLACES_PER_DAY = 2;
    private static final int HUB_SEED_LIMIT_PER_SIGNGU = 1;
    private static final int RELATED_LIMIT_PER_SEED = 3;
    private static final int MAX_PARALLEL_TARGET_BATCH = 6;
    private static final int MAX_BROAD_REGION_TARGETS = 8;
    private static final int MAX_NEARBY_FALLBACK_TARGETS = 2;
    private static final Duration FINAL_RESPONSE_CACHE_TTL = Duration.ofMinutes(30);

    private final TourApiClient tourApiClient;
    private final AreaCodeService areaCodeService;
    private final RegionFallbackService regionFallbackService;
    private final ExecutorService recommendationExecutor;
    private final RecommendationCacheService recommendationCacheService;

    public TripRecommendationService(TourApiClient tourApiClient,
                                     AreaCodeService areaCodeService,
                                     RegionFallbackService regionFallbackService,
                                     @Qualifier("recommendationExecutor") ExecutorService recommendationExecutor,
                                     RecommendationCacheService recommendationCacheService) {
        this.tourApiClient = tourApiClient;
        this.areaCodeService = areaCodeService;
        this.regionFallbackService = regionFallbackService;
        this.recommendationExecutor = recommendationExecutor;
        this.recommendationCacheService = recommendationCacheService;
    }

    public TripRecommendationResponse recommendCourse(String destination, int days) {
        if (!StringUtils.hasText(destination)) {
            throw new LlmCallException("여행 목적지는 필수입니다.");
        }

        if (days < 1) {
            throw new LlmCallException("여행 일수는 1일 이상이어야 합니다.");
        }

        String finalCacheKey = buildFinalCacheKey(destination, days);
        TripRecommendationResponse cached = recommendationCacheService.get(finalCacheKey);
        if (cached != null) {
            System.out.println("[TripRecommendation] FINAL CACHE HIT key=" + finalCacheKey);
            return cached;
        }

        long startTime = System.currentTimeMillis();
        int targetCount = days * PLACES_PER_DAY;

        ResolvedRegion resolvedRegion = areaCodeService.resolve(destination);
        List<RecommendedPlaceDto> mergedCandidates = new ArrayList<>();

        if (resolvedRegion.isSigunguScoped()) {
            RegionTarget primaryTarget = resolvedRegion.getTargets().get(0);
            List<RegionTarget> sameAreaTargets = areaCodeService.getTargetsByAreaName(primaryTarget.getAreaName());
            List<RegionTarget> fallbackTargets = regionFallbackService.buildFallbackTargets(primaryTarget, sameAreaTargets);

            mergedCandidates.addAll(collectCandidatesInParallelBatches(List.of(primaryTarget), targetCount));

            if (countUsefulUniquePlaces(mergedCandidates) < targetCount) {
                List<RegionTarget> nearbyTargets = fallbackTargets.stream()
                        .limit(MAX_NEARBY_FALLBACK_TARGETS)
                        .toList();

                mergedCandidates.addAll(collectCandidatesInParallelBatches(nearbyTargets, targetCount));
            }

        } else {
            List<RegionTarget> optimizedTargets = optimizeBroadTargets(resolvedRegion.getTargets());
            mergedCandidates.addAll(collectCandidatesInParallelBatches(optimizedTargets, targetCount));
        }

        if (mergedCandidates.isEmpty()) {
            throw new LlmCallException("TourAPI에서 추천할 장소를 찾지 못했습니다.");
        }

        List<RecommendedPlaceDto> selectedPlaces = selectFinalPlaces(
                mergedCandidates,
                resolvedRegion,
                targetCount
        );

        if (selectedPlaces.isEmpty()) {
            throw new LlmCallException("필터링 후 추천할 장소가 없습니다.");
        }

        List<DayCourseResponse> dayCourses = buildDayCourses(selectedPlaces, days);

        TripRecommendationResponse response = new TripRecommendationResponse(
                destination,
                days,
                selectedPlaces.size(),
                dayCourses
        );

        recommendationCacheService.put(finalCacheKey, response, FINAL_RESPONSE_CACHE_TTL);

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("[TripRecommendation] completed destination=" + destination
                + ", days=" + days
                + ", totalPlaces=" + selectedPlaces.size()
                + ", elapsedMs=" + elapsed);

        return response;
    }

    private List<RegionTarget> optimizeBroadTargets(List<RegionTarget> targets) {
        if (targets == null || targets.isEmpty()) {
            return List.of();
        }

        if (targets.size() <= MAX_BROAD_REGION_TARGETS) {
            return targets;
        }

        return targets.stream()
                .limit(MAX_BROAD_REGION_TARGETS)
                .toList();
    }

    private List<RecommendedPlaceDto> collectCandidatesInParallelBatches(List<RegionTarget> targets,
                                                                         int targetCount) {
        if (targets == null || targets.isEmpty()) {
            return List.of();
        }

        List<RecommendedPlaceDto> mergedCandidates = new ArrayList<>();
        AtomicBoolean stopFlag = new AtomicBoolean(false);

        for (int start = 0; start < targets.size() && !stopFlag.get(); start += MAX_PARALLEL_TARGET_BATCH) {
            int end = Math.min(start + MAX_PARALLEL_TARGET_BATCH, targets.size());
            List<RegionTarget> batch = targets.subList(start, end);

            List<CompletableFuture<List<RecommendedPlaceDto>>> futures = new ArrayList<>();

            for (RegionTarget target : batch) {
                if (stopFlag.get()) {
                    break;
                }

                CompletableFuture<List<RecommendedPlaceDto>> future =
                        CompletableFuture.supplyAsync(
                                () -> fetchCandidatesForTarget(target, stopFlag),
                                recommendationExecutor
                        ).exceptionally(ex -> {
                            System.out.println("[TripRecommendation] target task failed areaCd="
                                    + target.getAreaCode()
                                    + ", sigunguCd=" + target.getSigunguCode()
                                    + ", message=" + ex.getMessage());
                            return List.of();
                        });

                futures.add(future);
            }

            for (CompletableFuture<List<RecommendedPlaceDto>> future : futures) {
                mergedCandidates.addAll(future.join());
            }

            int usefulUniqueCount = countUsefulUniquePlaces(mergedCandidates);
            if (usefulUniqueCount >= targetCount) {
                stopFlag.set(true);
            }
        }

        return mergedCandidates;
    }

    private List<RecommendedPlaceDto> fetchCandidatesForTarget(RegionTarget target,
                                                               AtomicBoolean stopFlag) {
        if (stopFlag.get()) {
            return List.of();
        }

        List<RecommendedPlaceDto> result = new ArrayList<>();

        List<RecommendedPlaceDto> hubPlaces = tourApiClient.getHubPlaces(
                target.getAreaCode(),
                target.getSigunguCode()
        );

        if (hubPlaces.isEmpty()) {
            return result;
        }

        List<RecommendedPlaceDto> seedPlaces = hubPlaces.stream()
                .filter(place -> StringUtils.hasText(place.getPlaceName()))
                .filter(this::isUsefulPlace)
                .sorted(Comparator.comparingInt(this::rankOrMax))
                .limit(HUB_SEED_LIMIT_PER_SIGNGU)
                .toList();

        result.addAll(seedPlaces);

        for (RecommendedPlaceDto seed : seedPlaces) {
            if (stopFlag.get()) {
                break;
            }

            List<RecommendedPlaceDto> relatedPlaces = tourApiClient
                    .getRelatedPlacesByKeyword(
                            target.getAreaCode(),
                            target.getSigunguCode(),
                            seed.getPlaceName()
                    )
                    .stream()
                    .filter(this::isUsefulPlace)
                    .sorted(Comparator.comparingInt(this::rankOrMax))
                    .limit(RELATED_LIMIT_PER_SEED)
                    .toList();

            result.addAll(relatedPlaces);
        }

        return result;
    }

    private List<RecommendedPlaceDto> selectFinalPlaces(List<RecommendedPlaceDto> mergedCandidates,
                                                        ResolvedRegion resolvedRegion,
                                                        int targetCount) {

        List<RecommendedPlaceDto> filtered = mergedCandidates.stream()
                .filter(this::isUsefulPlace)
                .toList();

        List<RecommendedPlaceDto> unique = dedupPlaces(filtered);

        if (unique.isEmpty()) {
            return List.of();
        }

        if (!resolvedRegion.isSigunguScoped()) {
            return applyDiversity(unique, targetCount);
        }

        List<RecommendedPlaceDto> strict = unique.stream()
                .filter(place -> isSameSigungu(place, resolvedRegion.getRequestedSigunguName()))
                .toList();

        List<RecommendedPlaceDto> strictSelected = applyDiversity(strict, targetCount);

        if (strictSelected.size() >= targetCount / 2) {
            return strictSelected;
        }

        return applyDiversity(unique, targetCount);
    }

    private List<RecommendedPlaceDto> applyDiversity(List<RecommendedPlaceDto> candidates, int targetCount) {

        List<RecommendedPlaceDto> sorted = candidates.stream()
                .sorted(this::comparePlace)
                .toList();

        Map<String, Integer> addressCount = new HashMap<>();
        Map<String, Integer> categoryCount = new HashMap<>();

        List<RecommendedPlaceDto> selected = new ArrayList<>();

        for (RecommendedPlaceDto place : sorted) {

            String addressKey = extractSigungu(place.getAddressName());
            String categoryKey = normalize(place.getCategoryName());

            if (addressCount.getOrDefault(addressKey, 0) >= 2) {
                continue;
            }

            if (categoryCount.getOrDefault(categoryKey, 0) >= 3) {
                continue;
            }

            selected.add(place);

            addressCount.put(addressKey, addressCount.getOrDefault(addressKey, 0) + 1);
            categoryCount.put(categoryKey, categoryCount.getOrDefault(categoryKey, 0) + 1);

            if (selected.size() >= targetCount) {
                break;
            }
        }

        if (selected.size() < targetCount) {
            for (RecommendedPlaceDto place : sorted) {
                if (!selected.contains(place)) {
                    selected.add(place);
                    if (selected.size() >= targetCount) break;
                }
            }
        }

        return selected;
    }

    private String extractSigungu(String address) {
        if (address == null) return "";

        String normalized = normalize(address);

        String[] tokens = normalized.split(" ");

        if (tokens.length >= 2) {
            return tokens[1];
        }

        return normalized;
    }

    private boolean isSameSigungu(RecommendedPlaceDto place, String requestedSigunguName) {
        if (!StringUtils.hasText(requestedSigunguName)) {
            return false;
        }

        String address = normalize(place.getAddressName());
        String requested = normalize(requestedSigunguName);

        return address.contains(requested);
    }

    private int countUsefulUniquePlaces(List<RecommendedPlaceDto> places) {
        List<RecommendedPlaceDto> filtered = places.stream()
                .filter(this::isUsefulPlace)
                .toList();

        return dedupPlaces(filtered).size();
    }

    private String buildFinalCacheKey(String destination, int days) {
        return "trip:course:"
                + normalize(destination)
                + ":"
                + days
                + ":"
                + tourApiClient.getBaseYmVersionKey();
    }

    private boolean isUsefulPlace(RecommendedPlaceDto place) {
        String name = normalize(place.getPlaceName());
        String category = normalize(place.getCategoryName());

        if (!StringUtils.hasText(name)) {
            return false;
        }

        if (name.contains("백화점")
                || name.contains("역")
                || name.contains("터미널")
                || name.contains("공항")
                || name.contains("호텔")
                || name.contains("리조트")
                || name.contains("모텔")
                || name.contains("펜션")
                || name.contains("게스트하우스")
                || name.contains("카페")
                || name.contains("식당")
                || name.contains("맛집")
                || name.contains("회센터")
                || name.contains("반점")) {
            return false;
        }

        if (category.contains("쇼핑")
                || category.contains("숙박")
                || category.contains("음식")
                || category.contains("카페")
                || category.contains("간이음식")
                || category.contains("전문음식")
                || category.contains("외국식")) {
            return false;
        }

        return true;
    }

    private List<RecommendedPlaceDto> dedupPlaces(List<RecommendedPlaceDto> places) {
        Map<String, RecommendedPlaceDto> uniqueMap = new LinkedHashMap<>();

        for (RecommendedPlaceDto place : places) {
            String key = buildUniqueKey(place);
            RecommendedPlaceDto existing = uniqueMap.get(key);

            if (existing == null || comparePlace(place, existing) < 0) {
                uniqueMap.put(key, place);
            }
        }

        return new ArrayList<>(uniqueMap.values());
    }

    private int comparePlace(RecommendedPlaceDto a, RecommendedPlaceDto b) {
        int sourceScoreA = sourcePriority(a.getSource());
        int sourceScoreB = sourcePriority(b.getSource());

        if (sourceScoreA != sourceScoreB) {
            return Integer.compare(sourceScoreA, sourceScoreB);
        }

        return Integer.compare(rankOrMax(a), rankOrMax(b));
    }

    private int sourcePriority(String source) {
        if ("TOUR_API_RELATED".equals(source)) return 1;
        if ("TOUR_API_HUB".equals(source)) return 2;
        return 99;
    }

    private int rankOrMax(RecommendedPlaceDto place) {
        return place.getRank() == null ? Integer.MAX_VALUE : place.getRank();
    }

    private String buildUniqueKey(RecommendedPlaceDto place) {
        return normalize(place.getPlaceName()) + "|" + normalize(place.getAddressName());
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase()
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private List<DayCourseResponse> buildDayCourses(List<RecommendedPlaceDto> places, int days) {
        List<DayCourseResponse> dayCourses = new ArrayList<>();

        int totalPlaces = places.size();
        int base = totalPlaces / days;
        int remainder = totalPlaces % days;
        int index = 0;

        for (int day = 1; day <= days; day++) {
            int countForDay = base + (day <= remainder ? 1 : 0);
            List<RecommendedPlaceDto> dailyPlaces = new ArrayList<>();

            for (int i = 0; i < countForDay && index < totalPlaces; i++) {
                dailyPlaces.add(places.get(index++));
            }

            dayCourses.add(new DayCourseResponse(day, dailyPlaces));
        }

        return dayCourses;
    }
}