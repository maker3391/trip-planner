package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.client.TourApiClient;
import com.fiveguys.trip_planner.dto.RecommendedPlaceDto;
import com.fiveguys.trip_planner.exception.LlmCallException;
import com.fiveguys.trip_planner.response.DayCourseResponse;
import com.fiveguys.trip_planner.response.TripRecommendationResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TripRecommendationService {

    private static final int PLACES_PER_DAY = 2;
    private static final int HUB_SEED_LIMIT_PER_SIGNGU = 2;

    private final TourApiClient tourApiClient;
    private final AreaCodeService areaCodeService;

    public TripRecommendationService(TourApiClient tourApiClient,
                                     AreaCodeService areaCodeService) {
        this.tourApiClient = tourApiClient;
        this.areaCodeService = areaCodeService;
    }

    public TripRecommendationResponse recommendCourse(String destination, int days) {
        if (!StringUtils.hasText(destination)) {
            throw new LlmCallException("여행 목적지는 필수입니다.");
        }

        if (days < 1) {
            throw new LlmCallException("여행 일수는 1일 이상이어야 합니다.");
        }

        int targetCount = days * PLACES_PER_DAY;

        String areaCd = areaCodeService.getAreaCode(destination);
        List<String> signguCodes = areaCodeService.getSignguCodes(destination);

        if (!StringUtils.hasText(areaCd) || signguCodes.isEmpty()) {
            throw new LlmCallException("현재는 " + destination + " 지역 코드가 준비되지 않았습니다.");
        }

        List<RecommendedPlaceDto> mergedCandidates = new ArrayList<>();

        for (String signguCd : signguCodes) {
            List<RecommendedPlaceDto> hubPlaces = tourApiClient.getHubPlaces(areaCd, signguCd);

            List<RecommendedPlaceDto> seedPlaces = hubPlaces.stream()
                    .sorted(Comparator.comparingInt(this::rankOrMax))
                    .limit(HUB_SEED_LIMIT_PER_SIGNGU)
                    .toList();

            mergedCandidates.addAll(seedPlaces);

            for (RecommendedPlaceDto seed : seedPlaces) {
                if (!StringUtils.hasText(seed.getPlaceName())) {
                    continue;
                }

                List<RecommendedPlaceDto> relatedPlaces =
                        tourApiClient.getRelatedPlacesByKeyword(areaCd, signguCd, seed.getPlaceName());

                mergedCandidates.addAll(relatedPlaces);
            }
        }

        if (mergedCandidates.isEmpty()) {
            throw new LlmCallException("TourAPI에서 추천할 장소를 찾지 못했습니다.");
        }

        List<RecommendedPlaceDto> filtered = mergedCandidates.stream()
                .filter(this::isUsefulPlace)
                .collect(Collectors.toList());

        List<RecommendedPlaceDto> unique = dedupPlaces(filtered);

        List<RecommendedPlaceDto> sorted = unique.stream()
                .sorted(this::comparePlace)
                .limit(targetCount)
                .toList();

        if (sorted.isEmpty()) {
            throw new LlmCallException("필터링 후 추천할 장소가 없습니다.");
        }

        List<DayCourseResponse> dayCourses = buildDayCourses(sorted, days);

        return new TripRecommendationResponse(
                destination,
                days,
                sorted.size(),
                dayCourses
        );
    }

    private boolean isUsefulPlace(RecommendedPlaceDto place) {
        String name = normalize(place.getPlaceName());
        String category = normalize(place.getCategoryName());

        if (!StringUtils.hasText(name)) {
            return false;
        }

        if (name.contains("백화점") || name.contains("역") || name.contains("터미널")) {
            return false;
        }

        if (category.contains("쇼핑")) {
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