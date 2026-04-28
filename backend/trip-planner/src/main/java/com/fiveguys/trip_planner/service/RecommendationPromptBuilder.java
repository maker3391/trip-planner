package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.ItineraryRequestContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RecommendationPromptBuilder {

    public String buildItineraryPrompt(ItineraryRequestContext context) {
        String detailRule = buildDetailRule(context.getDetailArea());
        String scopeRule = buildScopeRule(context.getDestination());

        return """
                Return JSON only.

                {
                  "dayPlans": [
                    {
                      "day": 1,
                      "places": ["string", "string"]
                    }
                  ]
                }

                Rules:
                - use Korean place names only
                - output only real place names
                - each day has exactly 2 places
                - no explanation
                - do not use places outside the allowed destination scope
                - places within the same day must be geographically close to each other
                - avoid repeating the same type of place in consecutive days
                - each day should form a natural travel flow
                - do not repeat the same place across different days
                - prefer well-known landmarks, attractions, markets, parks, and beaches

                """ + scopeRule + detailRule + """

                Destination:
                %s

                Days:
                %d
                """.formatted(context.getDestination(), context.getDays());
    }

    private String buildDetailRule(String detailArea) {
        if (!StringUtils.hasText(detailArea)) {
            return "";
        }

        return """
                
                Focus area:
                - prioritize places in or near %s
                """.formatted(detailArea);
    }

    private String buildScopeRule(String destination) {
        if (!StringUtils.hasText(destination)) {
            return "";
        }

        String compact = destination.replaceAll("\\s+", "");

        if (compact.equals("서울") || compact.equals("서울특별시")) {
            return """
        
                    Allowed scope:
                    - use only places in 서울
                    - do not use places outside 서울
                    """;
        }

        if (compact.equals("경상북도")) {
            return """
                    
                    Allowed scope:
                    - use only places in 경상북도
                    - do not use places in 경상남도, 부산, 대구, 울산, or any other province
                    """;
        }

        if (compact.equals("경상남도")) {
            return """
                    
                    Allowed scope:
                    - use only places in 경상남도
                    - do not use places in 경상북도, 부산, 대구, 울산, or any other province
                    """;
        }

        if (compact.equals("전라북도")) {
            return """
                    
                    Allowed scope:
                    - use only places in 전라북도
                    - do not use places in 전라남도 or any other province
                    """;
        }

        if (compact.equals("전라남도")) {
            return """
                    
                    Allowed scope:
                    - use only places in 전라남도
                    - do not use places in 전라북도 or any other province
                    """;
        }

        if (compact.equals("충청북도")) {
            return """
                    
                    Allowed scope:
                    - use only places in 충청북도
                    - do not use places in 충청남도 or any other province
                    """;
        }

        if (compact.equals("충청남도")) {
            return """
                    
                    Allowed scope:
                    - use only places in 충청남도
                    - do not use places in 충청북도 or any other province
                    """;
        }

        if (compact.equals("경상도")) {
            return """
                    
                    Allowed scope:
                    - use only places in 경상북도 or 경상남도
                    - do not use places outside 경상권
                    """;
        }

        if (compact.equals("전라도")) {
            return """
                    
                    Allowed scope:
                    - use only places in 전라북도 or 전라남도
                    - do not use places outside 전라권
                    """;
        }

        if (compact.equals("충청도")) {
            return """
                    
                    Allowed scope:
                    - use only places in 충청북도 or 충청남도
                    - do not use places outside 충청권
                    """;
        }

        return "";
    }
}