package com.fiveguys.trip_planner.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiveguys.trip_planner.config.TourApiProperties;
import com.fiveguys.trip_planner.dto.TourApiPlaceCandidate;
import com.fiveguys.trip_planner.exception.LlmCallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Component
public class TourApiClient {

    private static final Logger log = LoggerFactory.getLogger(TourApiClient.class);

    private static final String CONTENT_TYPE_FOOD = "39";
    private static final String CONTENT_TYPE_STAY = "32";

    private final RestClient tourApiRestClient;
    private final TourApiProperties properties;
    private final ObjectMapper objectMapper;

    public TourApiClient(@Qualifier("tourApiRestClient") RestClient tourApiRestClient,
                         TourApiProperties properties,
                         ObjectMapper objectMapper) {
        this.tourApiRestClient = tourApiRestClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public List<TourApiPlaceCandidate> fetchRestaurants(String areaCode, String sigunguCode, int size) {
        return fetchByArea(areaCode, sigunguCode, CONTENT_TYPE_FOOD, size);
    }

    public List<TourApiPlaceCandidate> fetchStays(String areaCode, String sigunguCode, int size) {
        return fetchByArea(areaCode, sigunguCode, CONTENT_TYPE_STAY, size);
    }

    private List<TourApiPlaceCandidate> fetchByArea(String areaCode,
                                                    String sigunguCode,
                                                    String contentTypeId,
                                                    int size) {
        if (!StringUtils.hasText(areaCode)) {
            return List.of();
        }

        try {
            String raw = tourApiRestClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path(properties.getAreaListPath())
                                .queryParam("serviceKey", properties.getServiceKey())
                                .queryParam("MobileApp", properties.getMobileApp())
                                .queryParam("MobileOS", properties.getMobileOs())
                                .queryParam("_type", properties.getResponseType())
                                .queryParam("numOfRows", size)
                                .queryParam("pageNo", 1)
                                .queryParam("arrange", "P")
                                .queryParam("listYN", "Y")
                                .queryParam("areaCode", areaCode)
                                .queryParam("contentTypeId", contentTypeId);

                        if (StringUtils.hasText(sigunguCode)) {
                            builder.queryParam("sigunguCode", sigunguCode);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .body(String.class);

            log.info("TourAPI raw response: {}", raw);

            return parseItems(raw);

        } catch (RestClientResponseException e) {
            log.error("TourAPI HTTP error. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new LlmCallException("TourAPI HTTP 오류: " + e.getStatusCode() + " / " + e.getResponseBodyAsString(), e);
        } catch (LlmCallException e) {
            throw e;
        } catch (Exception e) {
            log.error("TourAPI call failed", e);
            throw new LlmCallException("TourAPI 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private List<TourApiPlaceCandidate> parseItems(String raw) throws Exception {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }

        JsonNode root = objectMapper.readTree(raw);

        JsonNode response = root.path("response");
        JsonNode header = response.path("header");
        String resultCode = header.path("resultCode").asText();
        String resultMsg = header.path("resultMsg").asText();

        if (StringUtils.hasText(resultCode) && !"0000".equals(resultCode)) {
            throw new LlmCallException("TourAPI 응답 오류: " + resultCode + " / " + resultMsg);
        }

        JsonNode itemsNode = response.path("body")
                .path("items")
                .path("item");

        List<TourApiPlaceCandidate> result = new ArrayList<>();

        if (itemsNode.isArray()) {
            for (JsonNode item : itemsNode) {
                result.add(new TourApiPlaceCandidate(
                        item.path("title").asText(null),
                        item.path("addr1").asText(null),
                        item.path("addr2").asText(null),
                        item.path("firstimage").asText(null),
                        item.path("contentid").asText(null)
                ));
            }
        } else if (!itemsNode.isMissingNode() && !itemsNode.isNull()) {
            result.add(new TourApiPlaceCandidate(
                    itemsNode.path("title").asText(null),
                    itemsNode.path("addr1").asText(null),
                    itemsNode.path("addr2").asText(null),
                    itemsNode.path("firstimage").asText(null),
                    itemsNode.path("contentid").asText(null)
            ));
        }

        return result;
    }
}