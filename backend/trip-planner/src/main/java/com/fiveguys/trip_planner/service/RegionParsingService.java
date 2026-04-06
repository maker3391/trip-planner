package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.ParsedRegion;
import com.fiveguys.trip_planner.dto.RegionScope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RegionParsingService {

    private static final Map<String, String> PROVINCE_ALIASES = new LinkedHashMap<>();
    private static final Map<String, String> CITY_TO_PROVINCE = new LinkedHashMap<>();

    static {
        PROVINCE_ALIASES.put("서울특별시", "서울");
        PROVINCE_ALIASES.put("서울시", "서울");
        PROVINCE_ALIASES.put("서울", "서울");

        PROVINCE_ALIASES.put("부산광역시", "부산");
        PROVINCE_ALIASES.put("부산시", "부산");
        PROVINCE_ALIASES.put("부산", "부산");

        PROVINCE_ALIASES.put("대구광역시", "대구");
        PROVINCE_ALIASES.put("대구시", "대구");
        PROVINCE_ALIASES.put("대구", "대구");

        PROVINCE_ALIASES.put("인천광역시", "인천");
        PROVINCE_ALIASES.put("인천시", "인천");
        PROVINCE_ALIASES.put("인천", "인천");

        PROVINCE_ALIASES.put("광주광역시", "광주");
        PROVINCE_ALIASES.put("광주시", "광주");
        PROVINCE_ALIASES.put("광주", "광주");

        PROVINCE_ALIASES.put("대전광역시", "대전");
        PROVINCE_ALIASES.put("대전시", "대전");
        PROVINCE_ALIASES.put("대전", "대전");

        PROVINCE_ALIASES.put("울산광역시", "울산");
        PROVINCE_ALIASES.put("울산시", "울산");
        PROVINCE_ALIASES.put("울산", "울산");

        PROVINCE_ALIASES.put("제주특별자치도", "제주");
        PROVINCE_ALIASES.put("제주도", "제주");
        PROVINCE_ALIASES.put("제주", "제주");

        PROVINCE_ALIASES.put("전라남도", "전남");
        PROVINCE_ALIASES.put("전남", "전남");
        PROVINCE_ALIASES.put("전라도", "전라도");

        PROVINCE_ALIASES.put("전라북도", "전북");
        PROVINCE_ALIASES.put("전북", "전북");

        PROVINCE_ALIASES.put("강원특별자치도", "강원");
        PROVINCE_ALIASES.put("강원도", "강원");
        PROVINCE_ALIASES.put("강원", "강원");

        PROVINCE_ALIASES.put("경상북도", "경북");
        PROVINCE_ALIASES.put("경북", "경북");

        PROVINCE_ALIASES.put("경상남도", "경남");
        PROVINCE_ALIASES.put("경남", "경남");

        CITY_TO_PROVINCE.put("여수", "전남");
        CITY_TO_PROVINCE.put("순천", "전남");
        CITY_TO_PROVINCE.put("목포", "전남");

        CITY_TO_PROVINCE.put("전주", "전북");
        CITY_TO_PROVINCE.put("군산", "전북");

        CITY_TO_PROVINCE.put("강릉", "강원");
        CITY_TO_PROVINCE.put("속초", "강원");

        CITY_TO_PROVINCE.put("경주", "경북");
        CITY_TO_PROVINCE.put("포항", "경북");

        CITY_TO_PROVINCE.put("창원", "경남");
        CITY_TO_PROVINCE.put("통영", "경남");
        CITY_TO_PROVINCE.put("거제", "경남");

        CITY_TO_PROVINCE.put("서울", "서울");
        CITY_TO_PROVINCE.put("부산", "부산");
        CITY_TO_PROVINCE.put("대구", "대구");
        CITY_TO_PROVINCE.put("인천", "인천");
        CITY_TO_PROVINCE.put("광주", "광주");
        CITY_TO_PROVINCE.put("대전", "대전");
        CITY_TO_PROVINCE.put("울산", "울산");
        CITY_TO_PROVINCE.put("제주", "제주");
    }

    public ParsedRegion parse(String originalMessage, String destination) {
        String merged = normalize(originalMessage + " " + destination);
        String normalizedDestination = normalize(destination);

        String district = extractDistrict(merged, normalizedDestination);
        String city = extractCity(merged, normalizedDestination);
        String province = extractProvince(merged, normalizedDestination);

        if (!StringUtils.hasText(province) && StringUtils.hasText(city)) {
            province = CITY_TO_PROVINCE.get(city);
        }

        RegionScope scope = resolveScope(province, city, district);
        String displayDestination = resolveDisplayDestination(province, city, district, destination);

        return new ParsedRegion(province, city, district, scope, displayDestination);
    }

    private String extractProvince(String merged, String destination) {
        for (Map.Entry<String, String> entry : PROVINCE_ALIASES.entrySet()) {
            String key = normalize(entry.getKey());
            if (merged.contains(key) || destination.contains(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String extractCity(String merged, String destination) {
        for (String city : CITY_TO_PROVINCE.keySet()) {
            String key = normalize(city);
            if (merged.contains(key) || destination.contains(key)) {
                return city;
            }
        }

        if (destination.endsWith("시")) {
            return destination.substring(0, destination.length() - 1);
        }

        if (destination.endsWith("군")) {
            return destination;
        }

        return null;
    }

    private String extractDistrict(String merged, String destination) {
        String[] tokens = (merged + " " + destination).split("\\s+");
        for (String token : tokens) {
            if (token.endsWith("구")) {
                return token;
            }
        }
        return null;
    }

    private RegionScope resolveScope(String province, String city, String district) {
        if (StringUtils.hasText(district)) {
            return RegionScope.DISTRICT;
        }
        if (StringUtils.hasText(city)) {
            return RegionScope.CITY;
        }
        if (StringUtils.hasText(province)) {
            return RegionScope.PROVINCE;
        }
        return RegionScope.UNKNOWN;
    }

    private String resolveDisplayDestination(String province,
                                             String city,
                                             String district,
                                             String fallbackDestination) {
        if (StringUtils.hasText(district) && StringUtils.hasText(city)) {
            return city + " " + district;
        }
        if (StringUtils.hasText(city)) {
            return city;
        }
        if (StringUtils.hasText(province)) {
            return province;
        }
        return fallbackDestination;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase()
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}