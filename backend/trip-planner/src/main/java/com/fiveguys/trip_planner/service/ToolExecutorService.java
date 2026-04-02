package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.ToolCallArguments;
import com.fiveguys.trip_planner.dto.ToolCallDto;
import com.fiveguys.trip_planner.exception.LlmCallException;
import com.fiveguys.trip_planner.response.TripRecommendationResponse;
import org.springframework.stereotype.Service;

@Service
public class ToolExecutorService {

    private final TripRecommendationService tripRecommendationService;

    public ToolExecutorService(TripRecommendationService tripRecommendationService) {
        this.tripRecommendationService = tripRecommendationService;
    }

    public TripRecommendationResponse execute(ToolCallDto toolCall) {
        if (toolCall == null) {
            throw new LlmCallException("도구 호출 정보가 존재하지 않습니다.");
        }

        if (!"recommend_trip_course".equals(toolCall.getTool())) {
            throw new LlmCallException("지원하지 않는 도구 호출입니다.");
        }

        ToolCallArguments arguments = toolCall.getArguments();
        if (arguments == null) {
            throw new LlmCallException("도구 호출 인자가 존재하지 않습니다.");
        }

        return tripRecommendationService.recommendCourse(
                arguments.getDestination(),
                arguments.getDays()
        );
    }
}