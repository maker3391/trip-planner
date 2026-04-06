package com.fiveguys.trip_planner.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiveguys.trip_planner.config.OpenAiProperties;
import com.fiveguys.trip_planner.dto.RecommendationDraft;
import com.fiveguys.trip_planner.exception.LlmCallException;
import com.fiveguys.trip_planner.service.RecommendationPromptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private final RestClient restClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RecommendationPromptBuilder promptBuilder;

    public OpenAiClient(@Qualifier("openAiRestClient") RestClient restClient,
                        OpenAiProperties properties,
                        ObjectMapper objectMapper,
                        RecommendationPromptBuilder promptBuilder) {
        this.restClient = restClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.promptBuilder = promptBuilder;
    }

    public RecommendationDraft generateRecommendationDraft(String userMessage) {
        try {
            String prompt = promptBuilder.build(userMessage);

            Map<String, Object> body = Map.of(
                    "model", properties.getModel(),
                    "input", prompt,
                    "reasoning", Map.of(
                            "effort", "low"
                    ),
                    "text", Map.of(
                            "format", Map.of(
                                    "type", "json_object"
                            )
                    ),
                    "max_output_tokens", properties.getMaxOutputTokens()
            );

            String raw = restClient.post()
                    .uri("/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            if (raw == null || raw.isBlank()) {
                throw new LlmCallException("추천 결과가 비어 있습니다.");
            }

            log.info("OpenAI raw: {}", raw);

            String json = extractJson(raw);

            return objectMapper.readValue(json, RecommendationDraft.class);

        } catch (HttpClientErrorException e) {
            throw new LlmCallException(resolveErrorMessage(e), e);
        } catch (LlmCallException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmCallException("추천 결과 처리 중 오류가 발생했습니다.", e);
        }
    }

    private String resolveErrorMessage(HttpClientErrorException e) {
        String body = e.getResponseBodyAsString();

        try {
            JsonNode root = objectMapper.readTree(body);
            String apiMessage = root.path("error").path("message").asText("");

            if (apiMessage.contains("Web Search cannot be used with JSON mode")) {
                return "웹 검색과 JSON 모드는 함께 사용할 수 없습니다.";
            }

            if (apiMessage.contains("temperature")) {
                return "현재 모델에서는 temperature 옵션을 사용할 수 없습니다.";
            }

            if (apiMessage.contains("max_output_tokens")) {
                return "출력 토큰 설정이 올바르지 않습니다.";
            }

            if (!apiMessage.isBlank()) {
                return "추천 요청 처리 중 오류가 발생했습니다: " + apiMessage;
            }
        } catch (Exception ignored) {
        }

        return "AI 호출 중 오류가 발생했습니다.";
    }

    private String extractJson(String raw) throws Exception {
        JsonNode root = objectMapper.readTree(raw);

        String status = root.path("status").asText();
        if ("incomplete".equals(status)) {
            String reason = root.path("incomplete_details").path("reason").asText();

            if ("max_output_tokens".equals(reason)) {
                throw new LlmCallException("추천 결과 생성 중 출력 길이 제한에 도달했습니다. max_output_tokens 값을 늘려주세요.");
            }

            throw new LlmCallException("추천 결과 생성이 중간에 종료되었습니다.");
        }

        JsonNode output = root.path("output");
        if (output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.path("content");
                if (!content.isArray()) {
                    continue;
                }

                for (JsonNode c : content) {
                    if ("output_text".equals(c.path("type").asText())) {
                        String text = c.path("text").asText();
                        if (!text.isBlank()) {
                            return text;
                        }
                    }
                }
            }
        }

        throw new LlmCallException("JSON 응답을 추출하지 못했습니다.");
    }
}