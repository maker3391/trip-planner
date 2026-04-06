package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class DetailAreaParsingService {

    private static final Map<String, String> DETAIL_AREA_ALIASES = new LinkedHashMap<>();
    private static final Map<String, String> DETAIL_AREA_PARENT_CITY = new LinkedHashMap<>();
    private static final Map<String, Set<String>> NEARBY_KEYWORDS = new LinkedHashMap<>();

    static {
        register("서면", "부산", "서면", "seomyeon");
        register("전포", "부산", "전포", "전포동", "jeonpo");
        register("남포동", "부산", "남포동", "남포", "nampo", "nampo-dong");
        register("해운대", "부산", "해운대", "해운대구", "haeundae");
        register("광안리", "부산", "광안리", "광안", "gwangalli");
        register("연남동", "서울", "연남동", "연남", "yeonnam");
        register("홍대", "서울", "홍대", "홍대입구", "hongdae");
        register("합정", "서울", "합정", "hapjeong");
        register("상수", "서울", "상수", "sangsu");
        register("강남", "서울", "강남", "강남구", "gangnam");
        register("명동", "서울", "명동", "myeongdong");
        register("종로", "서울", "종로", "종로구", "jongno");
        register("익선동", "서울", "익선동", "ikseon");
        register("성수", "서울", "성수", "성수동", "seongsu");
        register("잠실", "서울", "잠실", "jamsil");
        register("애월", "제주", "애월", "aewol");

        nearby("서면", "서면", "전포", "전포카페거리", "부전", "범내골", "부산시민공원");
        nearby("전포", "전포", "전포카페거리", "서면", "부전", "범내골");
        nearby("남포동", "남포동", "자갈치", "국제시장", "광복로", "용두산공원", "보수동", "중앙동");
        nearby("해운대", "해운대", "달맞이길", "미포", "청사포", "마린시티", "동백섬", "해운대해수욕장");
        nearby("광안리", "광안리", "광안대교", "민락", "수변공원", "광안리해수욕장");
        nearby("홍대", "홍대", "연남", "연남동", "상수", "합정", "망원");
        nearby("연남동", "연남동", "연남", "홍대", "합정", "상수");
        nearby("합정", "합정", "홍대", "상수", "망원", "연남");
        nearby("상수", "상수", "합정", "홍대", "연남");
        nearby("강남", "강남", "신논현", "역삼", "선릉", "압구정", "삼성");
        nearby("명동", "명동", "을지로", "충무로", "남산", "시청");
        nearby("종로", "종로", "익선동", "인사동", "광화문", "경복궁", "북촌");
        nearby("익선동", "익선동", "종로", "인사동", "광화문", "북촌");
        nearby("성수", "성수", "서울숲", "뚝섬", "성수동");
        nearby("잠실", "잠실", "석촌호수", "롯데월드", "송리단길");
        nearby("애월", "애월", "한담해안산책로", "곽지", "협재");
    }

    public String extractDetailArea(String text) {
        String normalized = normalize(text);

        for (Map.Entry<String, String> entry : DETAIL_AREA_ALIASES.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    public String resolveParentCity(String detailArea) {
        if (!StringUtils.hasText(detailArea)) {
            return null;
        }
        return DETAIL_AREA_PARENT_CITY.get(normalize(detailArea));
    }

    public Set<String> getNearbyKeywords(String detailArea) {
        if (!StringUtils.hasText(detailArea)) {
            return Set.of();
        }
        Set<String> keywords = NEARBY_KEYWORDS.get(normalize(detailArea));
        return keywords == null ? Set.of() : keywords;
    }

    public boolean matchesNearby(String detailArea, String value) {
        if (!StringUtils.hasText(detailArea) || !StringUtils.hasText(value)) {
            return false;
        }

        String normalizedValue = normalize(value);
        for (String keyword : getNearbyKeywords(detailArea)) {
            if (normalizedValue.contains(normalize(keyword))) {
                return true;
            }
        }

        return normalizedValue.contains(normalize(detailArea));
    }

    private static void register(String canonical, String parentCity, String... aliases) {
        DETAIL_AREA_PARENT_CITY.put(normalizeStatic(canonical), parentCity);
        for (String alias : aliases) {
            DETAIL_AREA_ALIASES.put(normalizeStatic(alias), canonical);
        }
    }

    private static void nearby(String canonical, String... keywords) {
        Set<String> values = new LinkedHashSet<>();
        for (String keyword : keywords) {
            values.add(keyword);
        }
        NEARBY_KEYWORDS.put(normalizeStatic(canonical), values);
    }

    private String normalize(String value) {
        return normalizeStatic(value);
    }

    private static String normalizeStatic(String value) {
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