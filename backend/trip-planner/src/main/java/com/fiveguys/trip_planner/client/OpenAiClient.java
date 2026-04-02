package com.fiveguys.trip_planner.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiveguys.trip_planner.config.OpenAiProperties;
import com.fiveguys.trip_planner.dto.ToolCallDto;
import com.fiveguys.trip_planner.exception.LlmCallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private static final String SYSTEM_PROMPT = """
            You are an intent parser for a travel planner backend.
            Your only job is to convert the user's Korean request into one JSON object.

            Rules:
            1. Return ONLY valid JSON.
            2. Do NOT return markdown.
            3. Do NOT explain anything.
            4. Do NOT generate travel descriptions.
            5. The only allowed tool is "recommend_trip_course".
            6. Output format must be exactly:
               {
                 "type": "tool_call",
                 "tool": "recommend_trip_course",
                 "arguments": {
                   "destination": "string",
                   "days": number
                 }
               }
            7. Normalize expressions:
               - "2박3일" -> days = 3
               - "3일" -> days = 3
               - "서울 2박3일 추천 일정 만들어줘" -> destination = "서울", days = 3
               - "부산 3일 코스 추천" -> destination = "부산", days = 3
            8. destination must be a Korean city or region from the user request.
            9. days must be an integer >= 1.
            10. Return JSON only. No extra text.
            """;

    private final RestClient restClient;
    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;

    public OpenAiClient(RestClient openAiRestClient,
                        OpenAiProperties openAiProperties,
                        ObjectMapper objectMapper) {
        this.restClient = openAiRestClient;
        this.openAiProperties = openAiProperties;
        this.objectMapper = objectMapper;
    }

    public ToolCallDto requestToolCall(String userMessage) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", openAiProperties.getModel(),
                    "input", List.of(
                            Map.of(
                                    "role", "developer",
                                    "content", List.of(
                                            Map.of(
                                                    "type", "input_text",
                                                    "text", SYSTEM_PROMPT
                                            )
                                    )
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of(
                                                    "type", "input_text",
                                                    "text", userMessage
                                            )
                                    )
                            )
                    ),
                    "reasoning", Map.of(
                            "effort", "minimal"
                    ),
                    "text", Map.of(
                            "format", Map.of(
                                    "type", "json_object"
                            )
                    ),
                    "max_output_tokens", openAiProperties.getMaxOutputTokens()
            );

            String rawResponse = restClient.post()
                    .uri("/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new LlmCallException("LLM 응답이 비어 있습니다.");
            }

            log.info("OpenAI raw response: {}", rawResponse);

            String jsonText = extractJsonText(rawResponse);
            log.info("Extracted tool_call JSON: {}", jsonText);

            return objectMapper.readValue(jsonText, ToolCallDto.class);

        } catch (LlmCallException e) {
            throw e;
        } catch (Exception e) {
            log.error("LLM 호출 또는 응답 파싱 중 오류가 발생했습니다.", e);
            throw new LlmCallException("LLM 호출 또는 응답 파싱 중 오류가 발생했습니다.", e);
        }
    }

    private String extractJsonText(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);

        String status = root.path("status").asText();
        if ("incomplete".equals(status)) {
            String reason = root.path("incomplete_details").path("reason").asText();
            throw new LlmCallException("LLM 응답이 토큰 제한으로 완료되지 않았습니다. reason=" + reason);
        }

        JsonNode outputTextNode = root.path("output_text");
        if (hasUsableText(outputTextNode)) {
            return outputTextNode.asText();
        }

        JsonNode outputNode = root.path("output");
        if (outputNode.isArray()) {
            for (JsonNode item : outputNode) {
                if (!"message".equals(item.path("type").asText())) {
                    continue;
                }

                JsonNode contentNode = item.path("content");
                if (contentNode.isArray()) {
                    for (JsonNode contentItem : contentNode) {
                        if ("output_text".equals(contentItem.path("type").asText())
                                && hasUsableText(contentItem.path("text"))) {
                            return contentItem.path("text").asText();
                        }
                    }
                }
            }
        }

        throw new LlmCallException("LLM 응답에서 JSON 본문을 추출할 수 없습니다.");
    }

    private boolean hasUsableText(JsonNode node) {
        return node != null
                && !node.isMissingNode()
                && !node.isNull()
                && node.isTextual()
                && !node.asText().isBlank();
    }
}