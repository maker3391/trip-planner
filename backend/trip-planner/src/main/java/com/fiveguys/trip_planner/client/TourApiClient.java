package com.fiveguys.trip_planner.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiveguys.trip_planner.config.TourApiProperties;
import com.fiveguys.trip_planner.dto.RecommendedPlaceDto;
import com.fiveguys.trip_planner.exception.LlmCallException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Component
public class TourApiClient {

    private final TourApiProperties tourApiProperties;
    private final ObjectMapper objectMapper;

    public TourApiClient(TourApiProperties tourApiProperties,
                         ObjectMapper objectMapper) {
        this.tourApiProperties = tourApiProperties;
        this.objectMapper = objectMapper;
    }

    public List<RecommendedPlaceDto> getHubPlaces(String areaCd, String signguCd) {
        return fetchHubWithFallback(areaCd, signguCd);
    }

    public List<RecommendedPlaceDto> getRelatedPlacesByKeyword(String areaCd, String signguCd, String keyword) {
        return fetchRelatedWithFallback(areaCd, signguCd, keyword);
    }

    private List<RecommendedPlaceDto> fetchHubWithFallback(String areaCd, String signguCd) {
        for (String baseYm : buildBaseYmCandidates()) {
            List<RecommendedPlaceDto> result = callHubApi(areaCd, signguCd, baseYm);
            if (!result.isEmpty()) {
                System.out.println("[TourAPI][HUB] SUCCESS baseYm=" + baseYm + ", signguCd=" + signguCd + ", count=" + result.size());
                return result;
            }
            System.out.println("[TourAPI][HUB] EMPTY baseYm=" + baseYm + ", signguCd=" + signguCd);
        }
        return List.of();
    }

    private List<RecommendedPlaceDto> fetchRelatedWithFallback(String areaCd, String signguCd, String keyword) {
        for (String baseYm : buildBaseYmCandidates()) {
            List<RecommendedPlaceDto> result = callRelatedApi(areaCd, signguCd, keyword, baseYm);
            if (!result.isEmpty()) {
                System.out.println("[TourAPI][RELATED] SUCCESS baseYm=" + baseYm + ", signguCd=" + signguCd + ", keyword=" + keyword + ", count=" + result.size());
                return result;
            }
            System.out.println("[TourAPI][RELATED] EMPTY baseYm=" + baseYm + ", signguCd=" + signguCd + ", keyword=" + keyword);
        }
        return List.of();
    }

    private List<String> buildBaseYmCandidates() {
        List<String> candidates = new ArrayList<>();
        candidates.add(YearMonth.now().minusMonths(1).toString().replace("-", ""));
        candidates.add(YearMonth.now().minusMonths(2).toString().replace("-", ""));
        candidates.add(YearMonth.now().minusMonths(3).toString().replace("-", ""));
        addIfAbsent(candidates, "202504");
        addIfAbsent(candidates, "202404");
        addIfAbsent(candidates, "202401");
        return candidates;
    }

    private void addIfAbsent(List<String> list, String value) {
        if (!list.contains(value)) {
            list.add(value);
        }
    }

    private List<RecommendedPlaceDto> callHubApi(String areaCd, String signguCd, String baseYm) {
        try {
            System.out.println("[TourAPI][HUB] hubBaseUrl=" + tourApiProperties.getHubBaseUrl());
            System.out.println("[TourAPI][HUB] serviceKey exists=" + StringUtils.hasText(tourApiProperties.getServiceKey()));
            System.out.println("[TourAPI][HUB] mobileOs=" + tourApiProperties.getMobileOs());
            System.out.println("[TourAPI][HUB] mobileApp=" + tourApiProperties.getMobileApp());

            String uri = UriComponentsBuilder
                    .fromUriString(tourApiProperties.getHubBaseUrl() + "/areaBasedList1")
                    .queryParam("serviceKey", tourApiProperties.getServiceKey())
                    .queryParam("numOfRows", 20)
                    .queryParam("pageNo", 1)
                    .queryParam("MobileOS", tourApiProperties.getMobileOs())
                    .queryParam("MobileApp", tourApiProperties.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("baseYm", baseYm)
                    .queryParam("areaCd", areaCd)
                    .queryParam("signguCd", signguCd)
                    .build(false)
                    .toUriString();

            System.out.println("[TourAPI][HUB] request uri=" + uri);

            String rawResponse = RestClient.create().get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            System.out.println("[TourAPI][HUB] raw response=" + rawResponse);

            if (!StringUtils.hasText(rawResponse)) {
                return List.of();
            }

            JsonNode root = objectMapper.readTree(rawResponse);
            String resultCode = root.path("response").path("header").path("resultCode").asText();
            String resultMsg = root.path("response").path("header").path("resultMsg").asText();

            System.out.println("[TourAPI][HUB] resultCode=" + resultCode + ", resultMsg=" + resultMsg);

            if (!"0000".equals(resultCode)) {
                return List.of();
            }

            JsonNode items = root.path("response").path("body").path("items").path("item");
            List<RecommendedPlaceDto> result = new ArrayList<>();

            if (items.isArray()) {
                for (JsonNode item : items) {
                    result.add(new RecommendedPlaceDto(
                            "TOUR_API_HUB",
                            item.path("hubTatsCd").asText(),
                            item.path("hubTatsNm").asText(),
                            buildHubCategory(item),
                            buildHubAddress(item),
                            "",
                            "",
                            "",
                            item.path("mapX").asText(),
                            item.path("mapY").asText(),
                            parseInteger(item.path("hubRank").asText())
                    ));
                }
            } else if (!items.isMissingNode() && !items.isNull() && items.isObject()) {
                result.add(new RecommendedPlaceDto(
                        "TOUR_API_HUB",
                        items.path("hubTatsCd").asText(),
                        items.path("hubTatsNm").asText(),
                        buildHubCategory(items),
                        buildHubAddress(items),
                        "",
                        "",
                        "",
                        items.path("mapX").asText(),
                        items.path("mapY").asText(),
                        parseInteger(items.path("hubRank").asText())
                ));
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new LlmCallException("TourAPI 중심 관광지 조회 중 오류가 발생했습니다.", e);
        }
    }

    private List<RecommendedPlaceDto> callRelatedApi(String areaCd, String signguCd, String keyword, String baseYm) {
        try {
            String uri = UriComponentsBuilder
                    .fromUriString(tourApiProperties.getRelatedBaseUrl() + "/searchKeyword1")
                    .queryParam("serviceKey", tourApiProperties.getServiceKey())
                    .queryParam("numOfRows", 10)
                    .queryParam("pageNo", 1)
                    .queryParam("MobileOS", tourApiProperties.getMobileOs())
                    .queryParam("MobileApp", tourApiProperties.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("baseYm", baseYm)
                    .queryParam("areaCd", areaCd)
                    .queryParam("signguCd", signguCd)
                    .queryParam("keyword", keyword)
                    .build(false)
                    .toUriString();

            String rawResponse = RestClient.create().get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            if (!StringUtils.hasText(rawResponse)) {
                return List.of();
            }

            JsonNode root = objectMapper.readTree(rawResponse);
            String resultCode = root.path("response").path("header").path("resultCode").asText();

            if (!"0000".equals(resultCode)) {
                return List.of();
            }

            JsonNode items = root.path("response").path("body").path("items").path("item");
            List<RecommendedPlaceDto> result = new ArrayList<>();

            if (items.isArray()) {
                for (JsonNode item : items) {
                    result.add(new RecommendedPlaceDto(
                            "TOUR_API_RELATED",
                            item.path("rlteTatsCd").asText(),
                            item.path("rlteTatsNm").asText(),
                            buildRelatedCategory(item),
                            buildRelatedAddress(item),
                            "",
                            "",
                            "",
                            "",
                            "",
                            parseInteger(item.path("rlteRank").asText())
                    ));
                }
            } else if (!items.isMissingNode() && !items.isNull() && items.isObject()) {
                result.add(new RecommendedPlaceDto(
                        "TOUR_API_RELATED",
                        items.path("rlteTatsCd").asText(),
                        items.path("rlteTatsNm").asText(),
                        buildRelatedCategory(items),
                        buildRelatedAddress(items),
                        "",
                        "",
                        "",
                        "",
                        "",
                        parseInteger(items.path("rlteRank").asText())
                ));
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new LlmCallException("TourAPI 연관 관광지 조회 중 오류가 발생했습니다.", e);
        }
    }

    private String buildHubCategory(JsonNode item) {
        String large = item.path("hubCtgryLclsNm").asText("");
        String medium = item.path("hubCtgryMclsNm").asText("");
        if (StringUtils.hasText(large) && StringUtils.hasText(medium)) {
            return large + " > " + medium;
        }
        return StringUtils.hasText(large) ? large : "관광지";
    }

    private String buildHubAddress(JsonNode item) {
        return (item.path("areaNm").asText("") + " " + item.path("signguNm").asText("")).trim();
    }

    private String buildRelatedCategory(JsonNode item) {
        String large = item.path("rlteCtgryLclsNm").asText("");
        String medium = item.path("rlteCtgryMclsNm").asText("");
        String small = item.path("rlteCtgrySclsNm").asText("");

        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(large)) sb.append(large);
        if (StringUtils.hasText(medium)) {
            if (!sb.isEmpty()) sb.append(" > ");
            sb.append(medium);
        }
        if (StringUtils.hasText(small)) {
            if (!sb.isEmpty()) sb.append(" > ");
            sb.append(small);
        }
        return sb.isEmpty() ? "관광지" : sb.toString();
    }

    private String buildRelatedAddress(JsonNode item) {
        return (item.path("rlteRegnNm").asText("") + " " + item.path("rlteSignguNm").asText("")).trim();
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }
}