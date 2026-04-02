package com.fiveguys.trip_planner.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiveguys.trip_planner.config.KakaoLocalProperties;
import com.fiveguys.trip_planner.dto.KakaoPlaceDto;
import com.fiveguys.trip_planner.exception.LlmCallException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
public class KakaoLocalClient {

    private final RestClient restClient;
    private final KakaoLocalProperties kakaoLocalProperties;
    private final ObjectMapper objectMapper;

    public KakaoLocalClient(RestClient kakaoLocalRestClient,
                            KakaoLocalProperties kakaoLocalProperties,
                            ObjectMapper objectMapper) {
        this.restClient = kakaoLocalRestClient;
        this.kakaoLocalProperties = kakaoLocalProperties;
        this.objectMapper = objectMapper;
    }

    public List<KakaoPlaceDto> searchKeywordPlaces(String query, int size) {
        try {
            String uri = UriComponentsBuilder.fromPath("/v2/local/search/keyword.json")
                    .queryParam("query", query)
                    .queryParam("size", size)
                    .build()
                    .toUriString();

            String rawResponse = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoLocalProperties.getRestApiKey())
                    .retrieve()
                    .body(String.class);

            if (!StringUtils.hasText(rawResponse)) {
                throw new LlmCallException("카카오 장소 검색 응답이 비어 있습니다.");
            }

            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode documents = root.path("documents");

            List<KakaoPlaceDto> places = new ArrayList<>();
            if (documents.isArray()) {
                for (JsonNode item : documents) {
                    places.add(new KakaoPlaceDto(
                            item.path("id").asText(),
                            item.path("place_name").asText(),
                            item.path("category_name").asText(),
                            item.path("address_name").asText(),
                            item.path("road_address_name").asText(),
                            item.path("phone").asText(),
                            item.path("place_url").asText(),
                            item.path("x").asText(),
                            item.path("y").asText()
                    ));
                }
            }

            return places;
        } catch (LlmCallException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmCallException("카카오 장소 검색 중 오류가 발생했습니다.", e);
        }
    }
}