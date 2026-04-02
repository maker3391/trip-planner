package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.client.OpenAiClient;
import com.fiveguys.trip_planner.dto.ChatRequest;
import com.fiveguys.trip_planner.dto.ToolCallArguments;
import com.fiveguys.trip_planner.dto.ToolCallDto;
import com.fiveguys.trip_planner.exception.LlmCallException;
import com.fiveguys.trip_planner.response.ChatResponse;
import com.fiveguys.trip_planner.response.TripRecommendationResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ChatService {

    private static final String SUPPORTED_TOOL = "recommend_trip_course";

    private final OpenAiClient openAiClient;
    private final ToolExecutorService toolExecutorService;

    public ChatService(OpenAiClient openAiClient,
                       ToolExecutorService toolExecutorService) {
        this.openAiClient = openAiClient;
        this.toolExecutorService = toolExecutorService;
    }

    public ChatResponse chat(ChatRequest request) {
        ToolCallDto toolCall = openAiClient.requestToolCall(request.getMessage());
        validateToolCall(toolCall);

        TripRecommendationResponse recommendation = toolExecutorService.execute(toolCall);

        return new ChatResponse(
                request.getMessage(),
                true,
                toolCall,
                recommendation
        );
    }

    private void validateToolCall(ToolCallDto toolCall) {
        if (toolCall == null) {
            throw new LlmCallException("도구 호출 정보가 존재하지 않습니다.");
        }

        if (!"tool_call".equals(toolCall.getType())) {
            throw new LlmCallException("도구 호출 타입이 올바르지 않습니다.");
        }

        if (!SUPPORTED_TOOL.equals(toolCall.getTool())) {
            throw new LlmCallException("지원하지 않는 도구 호출입니다.");
        }

        ToolCallArguments arguments = toolCall.getArguments();
        if (arguments == null) {
            throw new LlmCallException("도구 호출 인자가 존재하지 않습니다.");
        }

        if (!StringUtils.hasText(arguments.getDestination())) {
            throw new LlmCallException("여행 목적지는 필수입니다.");
        }

        if (arguments.getDays() == null || arguments.getDays() < 1) {
            throw new LlmCallException("여행 일수는 1일 이상이어야 합니다.");
        }
    }
}