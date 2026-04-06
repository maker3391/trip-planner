package com.fiveguys.trip_planner.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiveguys.trip_planner.config.OpenAiProperties;
import com.fiveguys.trip_planner.dto.RecommendationDraft;
import com.fiveguys.trip_planner.exception.LlmCallException;
import com.fiveguys.trip_planner.service.RecommendationPromptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private final RestClient restClient;
    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;
    private final RecommendationPromptBuilder promptBuilder;

    public OpenAiClient(RestClient openAiRestClient,
                        OpenAiProperties openAiProperties,
                        ObjectMapper objectMapper,
                        RecommendationPromptBuilder promptBuilder) {
        this.restClient = openAiRestClient;
        this.openAiProperties = openAiProperties;
        this.objectMapper = objectMapper;
        this.promptBuilder = promptBuilder;
    }

    public RecommendationDraft generateRecommendationDraft(String userMessage) {
        try {
            String prompt = promptBuilder.build(userMessage);

            Map<String, Object> requestBody = Map.of(
                    "model", openAiProperties.getModel(),
                    "input", prompt,
                    "tools", List.of(
                            Map.of(
                                    "type", "web_search_preview",
                                    "search_context_size", "low"
                            )
                    ),
                    "reasoning", Map.of(
                            "effort", "low"
                    ),
                    "text", Map.of(
                            "format", Map.of(
                                    "type", "json_schema",
                                    "name", "recommendation_draft",
                                    "strict", true,
                                    "schema", buildSchema()
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
                throw new LlmCallException("추천 결과가 비어 있습니다. 잠시 후 다시 시도해주세요.");
            }

            log.info("OpenAI raw response: {}", rawResponse);

            String jsonText = extractJsonText(rawResponse);
            log.info("Extracted recommendation JSON: {}", jsonText);

            return objectMapper.readValue(jsonText, RecommendationDraft.class);

        } catch (HttpClientErrorException e) {
            log.error("OpenAI 요청 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new LlmCallException(resolveOpenAiErrorMessage(e), e);

        } catch (LlmCallException e) {
            throw e;

        } catch (Exception e) {
            log.error("LLM 호출 또는 응답 파싱 중 오류가 발생했습니다.", e);
            throw new LlmCallException("추천 결과를 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }

    private String resolveOpenAiErrorMessage(HttpClientErrorException e) {
        String body = e.getResponseBodyAsString();

        try {
            JsonNode root = objectMapper.readTree(body);
            String apiMessage = root.path("error").path("message").asText("");
            String param = root.path("error").path("param").asText("");

            if (apiMessage.contains("cannot be used with reasoning.effort 'minimal'")) {
                return "추천 요청 옵션이 올바르지 않습니다. 웹 검색 설정과 추론 설정이 충돌했습니다.";
            }

            if ("tools[0].search_context_size".equals(param)) {
                return "웹 검색 컨텍스트 크기 설정이 올바르지 않습니다. low, medium, high 중 하나를 사용해야 합니다.";
            }

            if (apiMessage.contains("Invalid schema")) {
                return "추천 결과 형식 설정이 올바르지 않습니다. JSON 스키마를 다시 확인해주세요.";
            }

            if (apiMessage.contains("max_output_tokens")) {
                return "추천 결과 생성 중 응답 길이 제한에 도달했습니다. 출력 토큰 설정을 늘려주세요.";
            }

            if (apiMessage.contains("tools")) {
                return "추천 요청 도구 설정이 올바르지 않습니다. 웹 검색 도구 설정을 확인해주세요.";
            }

            if (!apiMessage.isBlank()) {
                return "추천 요청 처리 중 오류가 발생했습니다: " + apiMessage;
            }

        } catch (Exception parseException) {
            log.warn("OpenAI 에러 본문 파싱 실패: {}", body, parseException);
        }

        return "추천 요청 처리 중 외부 AI 호출 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
    }

    private Map<String, Object> buildSchema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "intent", Map.of(
                                "type", "string",
                                "enum", List.of(
                                        "TRAVEL_ITINERARY",
                                        "RESTAURANT_RECOMMENDATION",
                                        "STAY_RECOMMENDATION"
                                )
                        ),
                        "destination", Map.of(
                                "type", "string"
                        ),
                        "days", Map.of(
                                "type", List.of("integer", "null")
                        ),
                        "dayPlans", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "additionalProperties", false,
                                        "properties", Map.of(
                                                "day", Map.of("type", "integer"),
                                                "places", Map.of(
                                                        "type", "array",
                                                        "items", Map.of("type", "string")
                                                )
                                        ),
                                        "required", List.of("day", "places")
                                )
                        ),
                        "items", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "additionalProperties", false,
                                        "properties", Map.of(
                                                "name", Map.of("type", "string")
                                        ),
                                        "required", List.of("name")
                                )
                        )
                ),
                "required", List.of("intent", "destination", "days", "dayPlans", "items")
        );
    }

    private String extractJsonText(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);

        String status = root.path("status").asText();
        if ("incomplete".equals(status)) {
            String reason = root.path("incomplete_details").path("reason").asText();

            if ("max_output_tokens".equals(reason)) {
                throw new LlmCallException("추천 결과 생성 중 응답 길이 제한에 도달했습니다. 출력 토큰 설정을 늘려주세요.");
            }

            throw new LlmCallException("추천 결과 생성이 중간에 종료되었습니다. 잠시 후 다시 시도해주세요.");
        }

        JsonNode outputTextNode = root.path("output_text");
        if (hasUsableText(outputTextNode)) {
            return outputTextNode.asText();
        }

        JsonNode outputNode = root.path("output");
        if (outputNode.isArray()) {
            for (JsonNode item : outputNode) {
                JsonNode contentNode = item.path("content");
                if (!contentNode.isArray()) {
                    continue;
                }

                for (JsonNode contentItem : contentNode) {
                    if ("output_text".equals(contentItem.path("type").asText())
                            && hasUsableText(contentItem.path("text"))) {
                        return contentItem.path("text").asText();
                    }
                }
            }
        }

        throw new LlmCallException("추천 결과에서 JSON 본문을 추출하지 못했습니다.");
    }

    private boolean hasUsableText(JsonNode node) {
        return node != null
                && !node.isMissingNode()
                && !node.isNull()
                && node.isTextual()
                && !node.asText().isBlank();
    }
}