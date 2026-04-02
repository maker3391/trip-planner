package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.client.KakaoLocalClient;
import com.fiveguys.trip_planner.dto.KakaoPlaceDto;
import com.fiveguys.trip_planner.exception.LlmCallException;
import com.fiveguys.trip_planner.response.DayCourseResponse;
import com.fiveguys.trip_planner.response.TripRecommendationResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TripRecommendationService {

    private static final int PLACES_PER_DAY = 2;
    private static final int SEARCH_SIZE = 12;
    private static final int MIN_ACCEPTABLE_SCORE = 4;

    private static final List<String> CATEGORY_SELECTION_ORDER = List.of(
            "BEACH",
            "LANDMARK",
            "VILLAGE",
            "PARK",
            "MUSEUM",
            "OBSERVATORY",
            "ETC"
    );

    private static final List<String> SEARCH_QUERIES = List.of(
            "관광명소",
            "해수욕장",
            "유명 관광지",
            "공원",
            "문화마을"
    );

    private static final Map<String, List<KakaoPlaceDto>> CURATED_SEEDS = Map.of(
            "부산", List.of(
                    new KakaoPlaceDto("seed-busan-1", "해운대해수욕장", "여행 > 관광명소 > 해수욕장",
                            "부산 해운대구 우동", "", "", "", "129.1587", "35.1587"),
                    new KakaoPlaceDto("seed-busan-2", "광안리해수욕장", "여행 > 관광명소 > 해수욕장",
                            "부산 수영구 광안동", "", "", "", "129.1186", "35.1532"),
                    new KakaoPlaceDto("seed-busan-3", "감천문화마을", "여행 > 관광명소 > 문화마을",
                            "부산 사하구 감천동", "", "", "", "129.0106", "35.0972"),
                    new KakaoPlaceDto("seed-busan-4", "흰여울문화마을", "여행 > 관광명소 > 문화마을",
                            "부산 영도구 영선동4가", "", "", "", "129.0431", "35.0784"),
                    new KakaoPlaceDto("seed-busan-5", "송도해수욕장", "여행 > 관광명소 > 해수욕장",
                            "부산 서구 암남동", "", "", "", "129.0167", "35.0747"),
                    new KakaoPlaceDto("seed-busan-6", "BIFF광장", "여행 > 관광명소 > 광장",
                            "부산 중구 남포동", "", "", "", "129.0293", "35.0988")
            ),
            "서울", List.of(
                    new KakaoPlaceDto("seed-seoul-1", "경복궁", "여행 > 관광명소 > 고궁",
                            "서울 종로구 세종로", "", "", "", "126.9770", "37.5796"),
                    new KakaoPlaceDto("seed-seoul-2", "북촌한옥마을", "여행 > 관광명소 > 마을",
                            "서울 종로구 계동", "", "", "", "126.9849", "37.5826"),
                    new KakaoPlaceDto("seed-seoul-3", "남산서울타워", "여행 > 관광명소 > 전망대",
                            "서울 용산구 용산동2가", "", "", "", "126.9882", "37.5512"),
                    new KakaoPlaceDto("seed-seoul-4", "창덕궁", "여행 > 관광명소 > 고궁",
                            "서울 종로구 와룡동", "", "", "", "126.9910", "37.5794"),
                    new KakaoPlaceDto("seed-seoul-5", "청계천", "여행 > 관광명소 > 하천",
                            "서울 종로구 서린동", "", "", "", "126.9784", "37.5692"),
                    new KakaoPlaceDto("seed-seoul-6", "광화문광장", "여행 > 관광명소 > 광장",
                            "서울 종로구 세종로", "", "", "", "126.9769", "37.5726")
            ),
            "제주", List.of(
                    new KakaoPlaceDto("seed-jeju-1", "성산일출봉", "여행 > 관광명소 > 자연명소",
                            "제주 서귀포시 성산읍 성산리", "", "", "", "126.9405", "33.4589"),
                    new KakaoPlaceDto("seed-jeju-2", "섭지코지", "여행 > 관광명소 > 자연명소",
                            "제주 서귀포시 성산읍 고성리", "", "", "", "126.9295", "33.4240"),
                    new KakaoPlaceDto("seed-jeju-3", "협재해수욕장", "여행 > 관광명소 > 해수욕장",
                            "제주 제주시 한림읍 협재리", "", "", "", "126.2394", "33.3945"),
                    new KakaoPlaceDto("seed-jeju-4", "용두암", "여행 > 관광명소 > 자연명소",
                            "제주 제주시 용담이동", "", "", "", "126.5119", "33.5161"),
                    new KakaoPlaceDto("seed-jeju-5", "천지연폭포", "여행 > 관광명소 > 폭포",
                            "제주 서귀포시 천지동", "", "", "", "126.5540", "33.2471"),
                    new KakaoPlaceDto("seed-jeju-6", "우도", "여행 > 관광명소 > 섬",
                            "제주 제주시 우도면", "", "", "", "126.9542", "33.5001")
            )
    );

    private final KakaoLocalClient kakaoLocalClient;
    private final TripRecommendationRuleService tripRecommendationRuleService;

    public TripRecommendationService(KakaoLocalClient kakaoLocalClient,
                                     TripRecommendationRuleService tripRecommendationRuleService) {
        this.kakaoLocalClient = kakaoLocalClient;
        this.tripRecommendationRuleService = tripRecommendationRuleService;
    }

    public TripRecommendationResponse recommendCourse(String destination, int days) {
        if (!StringUtils.hasText(destination)) {
            throw new LlmCallException("여행 목적지는 필수입니다.");
        }

        if (days < 1) {
            throw new LlmCallException("여행 일수는 1일 이상이어야 합니다.");
        }

        int targetCount = days * PLACES_PER_DAY;
        Map<String, ScoredPlace> scoredPlaceMap = new LinkedHashMap<>();

        preloadCuratedSeeds(destination, scoredPlaceMap);

        for (String keyword : SEARCH_QUERIES) {
            String query = destination + " " + keyword;
            collectAndScorePlaces(scoredPlaceMap, query);
        }

        List<ScoredPlace> filteredPlaces = scoredPlaceMap.values().stream()
                .filter(scoredPlace -> scoredPlace.score() >= MIN_ACCEPTABLE_SCORE)
                .sorted(Comparator.comparingInt(ScoredPlace::score).reversed())
                .toList();

        List<KakaoPlaceDto> selectedPlaces = selectDiversePlaces(filteredPlaces, targetCount);

        if (selectedPlaces.size() < targetCount) {
            selectedPlaces = fillWithCuratedSeeds(destination, selectedPlaces, targetCount);
        }

        if (selectedPlaces.isEmpty()) {
            throw new LlmCallException("추천할 장소를 찾지 못했습니다.");
        }

        List<DayCourseResponse> dayCourses = buildDayCourses(selectedPlaces, days);

        return new TripRecommendationResponse(
                destination,
                days,
                selectedPlaces.size(),
                dayCourses
        );
    }

    private void preloadCuratedSeeds(String destination, Map<String, ScoredPlace> scoredPlaceMap) {
        List<KakaoPlaceDto> seeds = CURATED_SEEDS.getOrDefault(destination, List.of());

        for (KakaoPlaceDto seed : seeds) {
            String uniqueKey = tripRecommendationRuleService.buildNormalizedUniqueKey(seed);
            String categoryType = tripRecommendationRuleService.classifyCategory(seed);
            int score = tripRecommendationRuleService.scorePlace(seed) + 3;
            scoredPlaceMap.put(uniqueKey, new ScoredPlace(seed, score, categoryType));
        }
    }

    private void collectAndScorePlaces(Map<String, ScoredPlace> scoredPlaceMap, String query) {
        List<KakaoPlaceDto> places = kakaoLocalClient.searchKeywordPlaces(query, SEARCH_SIZE);

        for (KakaoPlaceDto place : places) {
            if (!StringUtils.hasText(place.getPlaceName())) {
                continue;
            }

            if (tripRecommendationRuleService.isExcludedPlace(place)) {
                continue;
            }

            int score = tripRecommendationRuleService.scorePlace(place);
            String uniqueKey = tripRecommendationRuleService.buildNormalizedUniqueKey(place);
            String categoryType = tripRecommendationRuleService.classifyCategory(place);

            ScoredPlace existing = scoredPlaceMap.get(uniqueKey);
            if (existing == null || score > existing.score()) {
                scoredPlaceMap.put(uniqueKey, new ScoredPlace(place, score, categoryType));
            }
        }
    }

    private List<KakaoPlaceDto> selectDiversePlaces(List<ScoredPlace> candidates, int targetCount) {
        Map<String, List<ScoredPlace>> grouped = new LinkedHashMap<>();

        for (String category : CATEGORY_SELECTION_ORDER) {
            grouped.put(category, new ArrayList<>());
        }

        for (ScoredPlace candidate : candidates) {
            grouped.computeIfAbsent(candidate.categoryType(), key -> new ArrayList<>()).add(candidate);
        }

        List<KakaoPlaceDto> selected = new ArrayList<>();
        Map<String, Integer> selectedCountByCategory = new LinkedHashMap<>();

        boolean pickedInRound = true;

        while (selected.size() < targetCount && pickedInRound) {
            pickedInRound = false;

            for (String category : CATEGORY_SELECTION_ORDER) {
                List<ScoredPlace> bucket = grouped.getOrDefault(category, List.of());
                if (bucket.isEmpty()) {
                    continue;
                }

                if (!tripRecommendationRuleService.canSelectCategory(category, selectedCountByCategory)) {
                    continue;
                }

                ScoredPlace picked = null;

                for (ScoredPlace candidate : bucket) {
                    if (!containsSimilarPlace(selected, candidate.place())) {
                        picked = candidate;
                        break;
                    }
                }

                if (picked != null) {
                    selected.add(picked.place());
                    selectedCountByCategory.put(category,
                            selectedCountByCategory.getOrDefault(category, 0) + 1);
                    bucket.remove(picked);
                    pickedInRound = true;

                    if (selected.size() >= targetCount) {
                        break;
                    }
                }
            }
        }

        if (selected.size() < targetCount) {
            for (ScoredPlace candidate : candidates) {
                if (selected.size() >= targetCount) {
                    break;
                }

                if (containsSimilarPlace(selected, candidate.place())) {
                    continue;
                }

                if (!tripRecommendationRuleService
                        .canSelectCategoryRelaxed(candidate.categoryType(), selectedCountByCategory)) {
                    continue;
                }

                selected.add(candidate.place());
                selectedCountByCategory.put(candidate.categoryType(),
                        selectedCountByCategory.getOrDefault(candidate.categoryType(), 0) + 1);
            }
        }

        return selected;
    }

    private boolean containsSimilarPlace(List<KakaoPlaceDto> selected, KakaoPlaceDto target) {
        for (KakaoPlaceDto existing : selected) {
            if (tripRecommendationRuleService.isSimilarPlace(existing, target)) {
                return true;
            }
        }
        return false;
    }

    private List<KakaoPlaceDto> fillWithCuratedSeeds(String destination,
                                                     List<KakaoPlaceDto> selectedPlaces,
                                                     int targetCount) {
        Map<String, KakaoPlaceDto> resultMap = new LinkedHashMap<>();
        Map<String, Integer> selectedCountByCategory = new LinkedHashMap<>();

        for (KakaoPlaceDto place : selectedPlaces) {
            resultMap.put(tripRecommendationRuleService.buildNormalizedUniqueKey(place), place);

            String categoryType = tripRecommendationRuleService.classifyCategory(place);
            selectedCountByCategory.put(categoryType,
                    selectedCountByCategory.getOrDefault(categoryType, 0) + 1);
        }

        List<KakaoPlaceDto> seeds = CURATED_SEEDS.getOrDefault(destination, List.of());
        for (KakaoPlaceDto seed : seeds) {
            if (resultMap.size() >= targetCount) {
                break;
            }

            boolean duplicated = resultMap.values().stream()
                    .anyMatch(existing -> tripRecommendationRuleService.isSimilarPlace(existing, seed));

            if (duplicated) {
                continue;
            }

            String categoryType = tripRecommendationRuleService.classifyCategory(seed);

            if (!tripRecommendationRuleService
                    .canSelectCategoryRelaxed(categoryType, selectedCountByCategory)) {
                continue;
            }

            resultMap.put(tripRecommendationRuleService.buildNormalizedUniqueKey(seed), seed);
            selectedCountByCategory.put(categoryType,
                    selectedCountByCategory.getOrDefault(categoryType, 0) + 1);
        }

        return resultMap.values().stream()
                .limit(targetCount)
                .toList();
    }

    private List<DayCourseResponse> buildDayCourses(List<KakaoPlaceDto> places, int days) {
        List<DayCourseResponse> dayCourses = new ArrayList<>();

        int totalPlaces = places.size();
        int base = totalPlaces / days;
        int remainder = totalPlaces % days;
        int index = 0;

        for (int day = 1; day <= days; day++) {
            int countForDay = base + (day <= remainder ? 1 : 0);
            List<KakaoPlaceDto> dailyPlaces = new ArrayList<>();

            for (int i = 0; i < countForDay && index < totalPlaces; i++) {
                dailyPlaces.add(places.get(index++));
            }

            dayCourses.add(new DayCourseResponse(day, dailyPlaces));
        }

        return dayCourses;
    }

    private record ScoredPlace(KakaoPlaceDto place, int score, String categoryType) {
    }
}