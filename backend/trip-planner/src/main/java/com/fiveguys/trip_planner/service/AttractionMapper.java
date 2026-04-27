package com.fiveguys.trip_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fiveguys.trip_planner.response.RecommendationItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AttractionMapper {

    private static final int MAX_ATTRACTION_ITEMS = 5;

    private final AttractionFilterService attractionFilterService;
    private final AttractionScoringService attractionScoringService;

    public AttractionMapper(AttractionFilterService attractionFilterService,
                            AttractionScoringService attractionScoringService) {
        this.attractionFilterService = attractionFilterService;
        this.attractionScoringService = attractionScoringService;
    }

    public List<RecommendationItemResponse> pickTopAttractions(List<JsonNode> docs,
                                                               String destination,
                                                               String detailArea,
                                                               String neighborhood,
                                                               String district) {
        return pickTopAttractions(
                docs,
                destination,
                detailArea,
                neighborhood,
                district,
                AttractionSubIntent.GENERAL
        );
    }

    public List<RecommendationItemResponse> pickTopAttractions(List<JsonNode> docs,
                                                               String destination,
                                                               String detailArea,
                                                               String neighborhood,
                                                               String district,
                                                               AttractionSubIntent subIntent) {
        List<ScoredPlace> scoredPlaces = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (JsonNode doc : docs) {
            if (!attractionFilterService.isAllowedAttraction(doc)) {
                continue;
            }

            if (!attractionFilterService.isLocationRelevant(doc, destination, detailArea, neighborhood, district)) {
                continue;
            }

            String name = AttractionTextHelper.clean(doc.path("place_name").asText());
            String address = chooseAddress(doc);
            String placeUrl = AttractionTextHelper.clean(doc.path("place_url").asText());
            String category = resolveAttractionCategory(doc);

            if (!StringUtils.hasText(name)) {
                continue;
            }

            if (attractionFilterService.looksLikeNoise(name, category)) {
                continue;
            }

            String dedupKey = buildDedupKey(name, address);

            if (!seen.add(dedupKey)) {
                continue;
            }

            int score = attractionScoringService.score(
                    doc,
                    destination,
                    detailArea,
                    neighborhood,
                    district,
                    subIntent
            );

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

    private String resolveAttractionCategory(JsonNode doc) {
        String categoryName = AttractionTextHelper.clean(doc.path("category_name").asText());

        if (!StringUtils.hasText(categoryName)) {
            return "명소";
        }

        String[] parts = categoryName.split(">");

        for (int i = parts.length - 1; i >= 0; i--) {
            String part = AttractionTextHelper.clean(parts[i]);

            if (!StringUtils.hasText(part)) {
                continue;
            }

            if ("여행".equals(part) || "관광,명소".equals(part)) {
                continue;
            }

            return part.split(",")[0].trim();
        }

        return "명소";
    }

    private String chooseAddress(JsonNode doc) {
        String roadAddress = AttractionTextHelper.clean(doc.path("road_address_name").asText());

        if (StringUtils.hasText(roadAddress)) {
            return roadAddress;
        }

        return AttractionTextHelper.clean(doc.path("address_name").asText());
    }

    private String buildDedupKey(String name, String address) {
        return (AttractionTextHelper.clean(name) + "|" + AttractionTextHelper.clean(address))
                .toLowerCase()
                .replaceAll("[\\s\\-_/()\\[\\],.]", "");
    }

    private record ScoredPlace(String name, String address, String placeUrl, String category, int score) {
    }
}