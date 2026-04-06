package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;

@Service
public class RecommendationPromptBuilder {

    public String build(String userMessage) {
        return """
        Return only valid JSON.

        Choose one intent:
        - TRAVEL_ITINERARY
        - RESTAURANT_RECOMMENDATION
        - STAY_RECOMMENDATION

        Rules:
        - destination must be in South Korea
        - Use Korean place names and Korean destination names
        - "2박3일" means days=3
        - TRAVEL_ITINERARY: days required, dayPlans count = days, each day has 2 to 4 places, items must be []
        - RESTAURANT_RECOMMENDATION: items 3 to 5, dayPlans must be []
        - STAY_RECOMMENDATION: items 3 to 5, dayPlans must be []
        - no markdown
        - no explanation

        User:
        """ + userMessage;
    }
}