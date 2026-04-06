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

                Required JSON structure:
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
                - use Korean place names and Korean destination names
                - destination must be in South Korea
                - intent must always be TRAVEL_ITINERARY
                - if a detailed area such as 서면, 남포동, 해운대, 홍대, 강남 is specified, destination must be the parent city and detailArea must be the exact detailed area
                - if no detailed area is specified, detailArea must be null
                - "2박3일" means days=3
                - days is required
                - dayPlans count must equal days
                - each dayPlan must be an object with fields day and places
                - each places has 2 to 4 strings
                - items must be []
                - when detailArea is specified, prioritize places in or near that detailArea
                - do not broaden to the entire city unless necessary
                - no nested arrays for dayPlans
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
                
                Detected detailed area from user message:
                - parent city: %s
                - detail area: %s
                - keep itinerary focused on %s and nearby spots
                """.formatted(parentCity, detailArea, detailArea);
    }
}