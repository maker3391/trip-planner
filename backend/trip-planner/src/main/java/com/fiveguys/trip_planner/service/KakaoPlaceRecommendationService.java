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

        validateRegionStrict(message);

        String destination = resolvedRegion.getCity();
        String district = resolvedRegion.getDistrict();
        String neighborhood = resolvedRegion.getNeighborhood();
        String detailArea = resolvedRegion.getDetailName();

        String queryDestination = resolveEffectiveDestination(destination, district);

        RegionAliasResolverService.ResolvedAlias alias = regionAliasResolverService.resolve(message, queryDestination);

        String aliasQueryHint = "";
        String aliasTargetName = "";
        String aliasTargetParent = "";

        if (alias != null) {
            if (StringUtils.hasText(alias.getCity())) {
                destination = alias.getCity();
                queryDestination = alias.getCity();
            }

            aliasQueryHint = alias.getQueryHint();
            aliasTargetName = alias.getTargetName();
            aliasTargetParent = alias.getTargetParent();

            if (StringUtils.hasText(aliasTargetParent)) {
                district = aliasTargetParent;
            }

            if (StringUtils.hasText(aliasTargetName)) {
                neighborhood = aliasTargetName;
                detailArea = aliasTargetName;
            }
        }

        String rawAreaHint = rawAreaHintExtractorService.extract(message, queryDestination);

        log.info("[REGION RESOLVED] city={}, district={}, neighborhood={}, detailArea={}, queryDestination={}, aliasHint={}, rawAreaHint={}",
                destination, district, neighborhood, detailArea, queryDestination, aliasQueryHint, rawAreaHint);

        validateResolvedDestination(queryDestination);

        String cacheKey = cacheKeyGenerator.generate(message);

        ChatResponse cached = recommendationCacheService.get(cacheKey);
        if (cached != null) {
            log.info("[KAKAO CACHE HIT] key={}", cacheKey);
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
            throw new LlmCallException("장소 검색 결과가 없습니다: " + buildDisplayDestination(queryDestination, detailArea, neighborhood, district, aliasQueryHint));
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
            throw new LlmCallException("추천 가능한 결과가 없습니다: " + buildDisplayDestination(queryDestination, detailArea, neighborhood, district, aliasQueryHint));
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

        recommendationCacheService.put(responseKey(cacheKey), response, resolveTtl(intent));
        return response;
    }

    private String responseKey(String cacheKey) {
        return cacheKey;
    }

    private void validateRegionStrict(String message) {
        if (!regionResolverService.hasExplicitTopLevelArea(message)) {
            throw new LlmCallException("지역은 반드시 앞에 포함해야 합니다. 예: 부산 중구 맛집 추천, 서울 성수동 카페 추천");
        }
    }

    private void validateResolvedDestination(String destination) {
        if (!StringUtils.hasText(destination)) {
            throw new LlmCallException("지역은 반드시 앞에 포함해야 합니다. 예: 부산 중구 맛집 추천, 서울 성수동 카페 추천");
        }
    }

    private String resolveEffectiveDestination(String destination, String district) {
        if (isCityOrCounty(district)) {
            return district;
        }
        return destination;
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
                    candidates.add(base + " 커피");
                } else if (containsKeyword(message, "술집")) {
                    candidates.add(base + " 술집");
                    candidates.add(base + " 주점");
                } else if (containsKeyword(message, "밥집", "점심")) {
                    candidates.add(base + " 식당");
                    candidates.add(base + " 한식");
                    candidates.add(base + " 밥집");
                } else {
                    candidates.add(base + " 맛집");
                    candidates.add(base + " 식당");
                    candidates.add(base + " 한식");
                    candidates.add(base + " 밥집");
                }
            }
        } else if ("STAY_RECOMMENDATION".equals(intent)) {
            String primaryStayKeyword = resolveStayKeyword(message);

            for (String base : locationBases) {
                candidates.add(base + " " + primaryStayKeyword);
                candidates.add(base + " 숙소");
                candidates.add(base + " 호텔");
                candidates.add(base + " 펜션");
                candidates.add(base + " 리조트");
                candidates.add(base + " 게스트하우스");
                candidates.add(base + " 민박");
                candidates.add(base + " 모텔");
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

        if (StringUtils.hasText(detailArea)) {
            bases.add(joinLocation(destination, detailArea));
        }

        if (StringUtils.hasText(neighborhood)) {
            bases.add(joinLocation(destination, neighborhood));
        }

        if (StringUtils.hasText(aliasTargetName)) {
            bases.add(joinLocation(destination, aliasTargetName));
        }

        if (StringUtils.hasText(aliasTargetParent) && StringUtils.hasText(aliasQueryHint)) {
            bases.add(joinLocation(destination, aliasTargetParent, aliasQueryHint));
        }

        if (StringUtils.hasText(aliasQueryHint)) {
            bases.add(joinLocation(destination, aliasQueryHint));
        }

        if (StringUtils.hasText(district) && !isCityOrCounty(district)) {
            bases.add(joinLocation(destination, district));
        }

        if (StringUtils.hasText(rawAreaHint)) {
            bases.add(joinLocation(destination, rawAreaHint));
        }

        bases.add(destination);

        return dedupStrings(bases);
    }

    private String joinLocation(String... parts) {
        List<String> values = new ArrayList<>();
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                values.add(part.trim());
            }
        }
        return String.join(" ", values).replaceAll("\\s+", " ").trim();
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

        if (StringUtils.hasText(destination) && !containsLooseRegion(merged, destination)) {
            return false;
        }

        boolean hasNarrowingCondition =
                StringUtils.hasText(detailArea)
                        || StringUtils.hasText(neighborhood)
                        || (StringUtils.hasText(district) && !isCityOrCounty(district))
                        || StringUtils.hasText(aliasQueryHint)
                        || StringUtils.hasText(aliasTargetParent);

        if (StringUtils.hasText(detailArea) && containsLooseRegion(merged, detailArea)) {
            return true;
        }

        if (StringUtils.hasText(neighborhood) && containsLooseRegion(merged, neighborhood)) {
            return true;
        }

        if (StringUtils.hasText(district) && !isCityOrCounty(district) && containsLooseRegion(merged, district)) {
            return true;
        }

        if (StringUtils.hasText(aliasQueryHint) && containsLooseRegion(merged, aliasQueryHint)) {
            return true;
        }

        if (StringUtils.hasText(aliasTargetParent) && containsLooseRegion(merged, aliasTargetParent)) {
            return true;
        }

        return !hasNarrowingCondition;
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
            if (containsLooseRegion(mergedAddress, detailArea)) score += 18;
            if (containsLooseRegion(name, detailArea)) score += 10;
        }

        if (StringUtils.hasText(neighborhood)) {
            if (containsLooseRegion(mergedAddress, neighborhood)) score += 15;
            if (containsLooseRegion(name, neighborhood)) score += 7;
        }

        if (StringUtils.hasText(district) && !isCityOrCounty(district)) {
            if (containsLooseRegion(mergedAddress, district)) score += 12;
            if (containsLooseRegion(name, district)) score += 5;
        }

        if (StringUtils.hasText(aliasQueryHint)) {
            if (containsLooseRegion(mergedAddress, aliasQueryHint)) score += 14;
            if (containsLooseRegion(name, aliasQueryHint)) score += 8;
        }

        if (StringUtils.hasText(aliasTargetParent)) {
            if (containsLooseRegion(mergedAddress, aliasTargetParent)) score += 9;
            if (containsLooseRegion(name, aliasTargetParent)) score += 4;
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
        if (StringUtils.hasText(detailArea)) {
            return joinLocation(destination, detailArea);
        }

        if (StringUtils.hasText(neighborhood)) {
            return joinLocation(destination, neighborhood);
        }

        if (StringUtils.hasText(aliasQueryHint)) {
            return joinLocation(destination, aliasQueryHint);
        }

        if (StringUtils.hasText(district) && !isCityOrCounty(district)) {
            return joinLocation(destination, district);
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
        return StringUtils.hasText(strippedKeyword) && normalizedValue.contains(strippedKeyword);
    }

    private String stripRegionSuffixForLooseMatch(String value) {
        return value.replaceAll("(특별자치도|특별자치시|광역시|특별시|시|군|구|동|읍|면|리)$", "").trim();
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