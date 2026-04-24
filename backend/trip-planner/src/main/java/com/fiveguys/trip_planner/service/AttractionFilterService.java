package com.fiveguys.trip_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class AttractionFilterService {

    public boolean isAllowedAttraction(JsonNode doc) {
        String categoryGroupCode = AttractionTextHelper.clean(doc.path("category_group_code").asText());
        String categoryName = AttractionTextHelper.clean(doc.path("category_name").asText());

        if ("AT4".equals(categoryGroupCode)) {
            return true;
        }

        return AttractionTextHelper.containsKeyword(categoryName,
                "관광명소", "문화시설", "유적", "박물관", "미술관", "공원",
                "전망대", "폭포", "해변", "산", "사찰", "궁", "랜드마크", "케이블카",
                "유원지", "테마파크", "수목원", "휴양림", "해수욕장", "먹자골목");
    }

    public boolean isLocationRelevant(JsonNode doc,
                                      String destination,
                                      String detailArea,
                                      String neighborhood,
                                      String district) {
        String name = AttractionTextHelper.clean(doc.path("place_name").asText());
        String roadAddress = AttractionTextHelper.clean(doc.path("road_address_name").asText());
        String addressName = AttractionTextHelper.clean(doc.path("address_name").asText());

        String merged = ((roadAddress == null ? "" : roadAddress) + " "
                + (addressName == null ? "" : addressName) + " "
                + (name == null ? "" : name)).toLowerCase();

        boolean destinationMatch = containsLooseRegion(merged, destination);
        boolean detailMatch = !StringUtils.hasText(detailArea) || containsLooseRegion(merged, detailArea);
        boolean neighborhoodMatch = !StringUtils.hasText(neighborhood) || containsLooseRegion(merged, neighborhood);
        boolean districtMatch = !StringUtils.hasText(district)
                || AttractionTextHelper.isCityOrCounty(district)
                || containsLooseRegion(merged, district);

        if (!destinationMatch) {
            return false;
        }

        if (StringUtils.hasText(detailArea)) {
            if (isTransportHub(detailArea)) {
                if (StringUtils.hasText(district) && !AttractionTextHelper.isCityOrCounty(district)) {
                    return districtMatch;
                }

                return destinationMatch;
            }

            return detailMatch;
        }

        if (StringUtils.hasText(neighborhood)) {
            return neighborhoodMatch;
        }

        if (StringUtils.hasText(district) && !AttractionTextHelper.isCityOrCounty(district)) {
            return districtMatch;
        }

        return destinationMatch;
    }

    public boolean looksLikeNoise(String name, String category) {
        if (!StringUtils.hasText(name)) {
            return true;
        }

        if (name.length() > 40) {
            return true;
        }

        if (AttractionTextHelper.containsKeyword(category,
                "숙박", "음식점", "카페", "주점", "유흥", "노래방")) {
            return true;
        }

        return AttractionTextHelper.containsKeyword(name,
                "호텔", "모텔", "펜션", "게스트하우스", "리조트",
                "맛집", "카페",
                "주차장", "관리사무소", "관리소", "안내소",
                "행정복지센터", "주민센터",
                "인증대", "중간인증대", "종점인증대",
                "매표소", "화장실", "출입구");
    }

    public boolean isTransportHub(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        return AttractionTextHelper.containsKeyword(
                value,
                "공항", "국제공항",
                "역", "터미널", "버스터미널", "기차역"
        );
    }

    public boolean containsLooseRegion(String value, String keyword) {
        if (!StringUtils.hasText(value) || !StringUtils.hasText(keyword)) {
            return false;
        }

        String normalizedValue = AttractionTextHelper.normalizeForMatch(value);
        String normalizedKeyword = AttractionTextHelper.normalizeForMatch(keyword);

        if (normalizedValue.equals(normalizedKeyword)) {
            return true;
        }

        if (normalizedValue.contains(normalizedKeyword)) {
            return true;
        }

        String strippedKeyword = AttractionTextHelper.stripRegionSuffixForLooseMatch(normalizedKeyword);

        if (StringUtils.hasText(strippedKeyword)
                && strippedKeyword.length() >= 2
                && normalizedValue.contains(strippedKeyword)) {
            return true;
        }

        for (String alias : expandRegionAliases(normalizedKeyword)) {
            if (normalizedValue.equals(alias) || normalizedValue.contains(alias)) {
                return true;
            }

            String strippedAlias = AttractionTextHelper.stripRegionSuffixForLooseMatch(alias);

            if (StringUtils.hasText(strippedAlias)
                    && strippedAlias.length() >= 2
                    && normalizedValue.contains(strippedAlias)) {
                return true;
            }
        }

        return false;
    }

    private List<String> expandRegionAliases(String keyword) {
        List<String> aliases = new ArrayList<>();
        aliases.add(keyword);

        String compact = keyword.replaceAll("\\s+", "");

        switch (compact) {
            case "경상북":
            case "경상북도":
            case "경북":
                aliases.add("경상북");
                aliases.add("경상북도");
                aliases.add("경북");
                break;
            case "경상남":
            case "경상남도":
            case "경남":
                aliases.add("경상남");
                aliases.add("경상남도");
                aliases.add("경남");
                break;
            case "전라북":
            case "전라북도":
            case "전북":
                aliases.add("전라북");
                aliases.add("전라북도");
                aliases.add("전북");
                break;
            case "전라남":
            case "전라남도":
            case "전남":
                aliases.add("전라남");
                aliases.add("전라남도");
                aliases.add("전남");
                break;
            case "충청북":
            case "충청북도":
            case "충북":
                aliases.add("충청북");
                aliases.add("충청북도");
                aliases.add("충북");
                break;
            case "충청남":
            case "충청남도":
            case "충남":
                aliases.add("충청남");
                aliases.add("충청남도");
                aliases.add("충남");
                break;
            case "제주":
            case "제주도":
            case "제주특별자치도":
                aliases.add("제주");
                aliases.add("제주도");
                aliases.add("제주특별자치도");
                break;
            case "강원":
            case "강원도":
            case "강원특별자치도":
                aliases.add("강원");
                aliases.add("강원도");
                aliases.add("강원특별자치도");
                break;
            default:
                break;
        }

        return aliases.stream().distinct().toList();
    }
}