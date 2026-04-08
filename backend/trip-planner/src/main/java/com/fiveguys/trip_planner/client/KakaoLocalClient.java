package com.fiveguys.trip_planner.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiveguys.trip_planner.config.KakaoLocalProperties;
import com.fiveguys.trip_planner.exception.LlmCallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class KakaoLocalClient {

    private static final Logger log = LoggerFactory.getLogger(KakaoLocalClient.class);

    private final RestClient restClient;
    private final KakaoLocalProperties properties;
    private final ObjectMapper objectMapper;

    public KakaoLocalClient(@Qualifier("kakaoLocalRestClient") RestClient restClient,
                            KakaoLocalProperties properties,
                            ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public JsonNode searchKeyword(String query) {
        try {
            String raw = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/keyword.json")
                            .queryParam("query", query)
                            .queryParam("size", 15)
                            .build())
                    .header("Authorization", "KakaoAK " + properties.getRestApiKey())
                    .retrieve()
                    .body(String.class);

            log.info("[KAKAO] query={}", query);
            log.debug("[KAKAO RAW] {}", raw);

            return objectMapper.readTree(raw);

        } catch (RestClientResponseException e) {
            throw new LlmCallException(
                    "Kakao API 오류: " + e.getStatusCode() + " / " + e.getResponseBodyAsString(),
                    e
            );
        } catch (Exception e) {
            throw new LlmCallException("Kakao API 호출 실패", e);
        }
    }
}