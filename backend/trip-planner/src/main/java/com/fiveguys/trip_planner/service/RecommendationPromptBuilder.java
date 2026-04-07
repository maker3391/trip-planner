package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RecommendationPromptBuilder {

    private final DetailAreaParsingService detailAreaParsingService;

    public RecommendationPromptBuilder(DetailAreaParsingService detailAreaParsingService) {
        this.detailAreaParsingService = detailAreaParsingService;
    }

    public String build(String userMessage) {
        String detailArea = detailAreaParsingService.extractDetailArea(userMessage);
        String parentCity = detailAreaParsingService.resolveParentCity(detailArea);

        String detailRule = buildDetailRule(detailArea, parentCity);

        return """
                Return JSON only.

                {
                  "intent": "TRAVEL_ITINERARY",
                  "destination": "string",
                  "detailArea": "string or null",
                  "days": integer,
                  "dayPlans": [
                    {
                      "day": integer,
                      "places": ["string", "string"]
                    }
                  ],
                  "items": []
                }

                Rules:
                - use Korean place names
                - destination must be in South Korea
                - intent must be TRAVEL_ITINERARY
                - if detailed area exists, destination must be the parent city and detailArea must be that area
                - if no detailed area exists, detailArea must be null
                - "2박3일" means days=3
                - dayPlans count must equal days
                - each day must have 2 to 4 places
                - items must be []
                - no markdown
                - no explanation
                """ + detailRule + """

                User:
                """ + userMessage;
    }

    private String buildDetailRule(String detailArea, String parentCity) {
        if (!StringUtils.hasText(detailArea) || !StringUtils.hasText(parentCity)) {
            return "";
        }

        return """
                
                Focus area:
                - parent city: %s
                - detail area: %s
                - prioritize places in or near %s
                """.formatted(parentCity, detailArea, detailArea);
    }
}