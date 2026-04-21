package com.fiveguys.trip_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fiveguys.trip_planner.response.RecommendationItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KakaoPlaceMapper {

    public RecommendationItemResponse toRecommendationItemResponse(String intent, JsonNode doc) {
        return new RecommendationItemResponse(
                text(doc, "place_name"),
                resolveAddress(doc),
                text(doc, "place_url"),
                resolveCategory(intent, doc)
        );
    }

    public String resolveAddress(JsonNode doc) {
        String road = text(doc, "road_address_name");
        if (StringUtils.hasText(road)) {
            return road;
        }
        return text(doc, "address_name");
    }

    public String resolveCategory(String intent, JsonNode doc) {
        String categoryName = text(doc, "category_name");
        if (!StringUtils.hasText(categoryName)) {
            return "STAY_RECOMMENDATION".equals(intent) ? "숙소" : "맛집";
        }

        if ("STAY_RECOMMENDATION".equals(intent)) {
            String[] parts = categoryName.split(">");
            for (int i = parts.length - 1; i >= 0; i--) {
                String part = parts[i].trim();
                if (StringUtils.hasText(part) && !"숙박".equals(part)) {
                    return part;
                }
            }
            return "숙소";
        }

        String[] parts = categoryName.split(">");
        for (int i = parts.length - 1; i >= 0; i--) {
            String part = parts[i].trim();
            if (!StringUtils.hasText(part) || isGenericRestaurantCategory(part)) {
                continue;
            }

            if (part.contains("카페")) {
                return "카페";
            }

            if (part.contains("주점")) {
                return "술집";
            }

            return part;
        }

        if (categoryName.contains("카페")) {
            return "카페";
        }
        if (categoryName.contains("주점")) {
            return "술집";
        }

        return "맛집";
    }

    private boolean isGenericRestaurantCategory(String value) {
        return "음식점".equals(value)
                || "식당".equals(value)
                || "레스토랑".equals(value)
                || "맛집".equals(value);
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isNull()) {
            return "";
        }
        return node.get(fieldName).asText("");
    }
}