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
        register("서면", "부산", "서면", "seomyeon", "서면역");
        register("전포", "부산", "전포", "전포동", "전포역", "전포카페거리", "jeonpo");
        register("남포동", "부산", "남포동", "남포", "남포역", "광복동", "광복로", "nampo", "nampo-dong");
        register("해운대", "부산", "해운대", "해운대구", "해운대역", "해운대해수욕장", "haeundae");
        register("광안리", "부산", "광안리", "광안", "광안동", "광안리해수욕장", "gwangalli");
        register("연남동", "서울", "연남동", "연남", "yeonnam");
        register("홍대", "서울", "홍대", "홍대입구", "홍대입구역", "hongdae");
        register("합정", "서울", "합정", "합정역", "hapjeong");
        register("상수", "서울", "상수", "상수역", "sangsu");
        register("강남", "서울", "강남", "강남구", "강남역", "gangnam");
        register("명동", "서울", "명동", "명동역", "myeongdong");
        register("종로", "서울", "종로", "종로구", "종각", "종각역", "jongno");
        register("익선동", "서울", "익선동", "ikseon");
        register("성수", "서울", "성수", "성수동", "성수역", "seongsu");
        register("잠실", "서울", "잠실", "잠실역", "jamsil");
        register("애월", "제주", "애월", "애월읍", "aewol");

        nearby("서면",
                "서면", "서면역", "전포", "전포동", "전포카페거리", "부전", "부전역",
                "범내골", "부산시민공원", "서면문화로", "서면젊음의거리", "서면먹자골목");

        nearby("전포",
                "전포", "전포동", "전포역", "전포카페거리", "서면", "서면역",
                "부전", "부전역", "범내골", "부산시민공원");

        nearby("남포동",
                "남포동", "남포", "남포역", "자갈치", "자갈치시장", "국제시장",
                "부평깡통시장", "광복로", "광복동", "용두산공원", "부산타워",
                "보수동", "보수동책방골목", "중앙동", "영도대교", "흰여울문화마을");

        nearby("해운대",
                "해운대", "해운대역", "해운대해수욕장", "달맞이길", "미포",
                "청사포", "청사포다릿돌전망대", "마린시티", "더베이101",
                "동백섬", "누리마루", "해리단길", "송정", "송정해수욕장",
                "블루라인파크", "해운대시장", "센텀시티", "벡스코");

        nearby("광안리",
                "광안리", "광안", "광안동", "광안리해수욕장", "광안대교",
                "민락", "민락동", "민락수변공원", "수변공원", "민락더마켓",
                "남천동", "금련산", "금련산청소년수련원", "금련산전망대",
                "황령산", "황령산전망대", "이기대", "오륙도", "삼익비치",
                "광안리카페거리", "광안리해변테마거리");

        nearby("홍대",
                "홍대", "홍대입구", "홍대입구역", "연남", "연남동", "상수",
                "상수역", "합정", "합정역", "망원", "망원동", "망원시장",
                "경의선숲길", "걷고싶은거리", "홍대거리");

        nearby("연남동",
                "연남동", "연남", "홍대", "홍대입구", "홍대입구역", "합정",
                "상수", "망원", "경의선숲길", "연트럴파크", "동진시장");

        nearby("합정",
                "합정", "합정역", "홍대", "홍대입구", "상수", "상수역",
                "망원", "망원동", "망원시장", "연남", "메세나폴리스");

        nearby("상수",
                "상수", "상수역", "합정", "합정역", "홍대", "홍대입구",
                "연남", "망원", "당인리", "한강공원");

        nearby("강남",
                "강남", "강남역", "신논현", "신논현역", "역삼", "역삼역",
                "선릉", "선릉역", "압구정", "압구정로데오", "삼성", "삼성역",
                "코엑스", "봉은사", "가로수길", "논현", "청담");

        nearby("명동",
                "명동", "명동역", "을지로", "을지로입구", "충무로", "남산",
                "남산타워", "N서울타워", "시청", "서울시청", "회현", "남대문",
                "남대문시장", "청계천", "롯데백화점본점");

        nearby("종로",
                "종로", "종로구", "종각", "종각역", "익선동", "인사동",
                "광화문", "경복궁", "북촌", "북촌한옥마을", "삼청동",
                "청계천", "창덕궁", "창경궁", "대학로", "혜화", "서촌",
                "통인시장", "광장시장");

        nearby("익선동",
                "익선동", "종로", "인사동", "광화문", "북촌", "북촌한옥마을",
                "삼청동", "낙원상가", "종묘", "창덕궁", "운현궁");

        nearby("성수",
                "성수", "성수동", "성수역", "서울숲", "뚝섬", "뚝섬역",
                "뚝섬유원지", "한강공원", "카페거리", "연무장길",
                "언더스탠드에비뉴", "아틀리에길");

        nearby("잠실",
                "잠실", "잠실역", "석촌호수", "롯데월드", "롯데월드타워",
                "서울스카이", "송리단길", "방이동", "방이먹자골목",
                "올림픽공원", "몽촌토성", "잠실한강공원", "롯데월드몰");

        nearby("애월",
                "애월", "애월읍", "한담해안산책로", "한담해변", "애월한담공원",
                "곽지", "곽지해수욕장", "협재", "협재해수욕장", "금능",
                "금능해수욕장", "새별오름", "고내리", "구엄리", "구엄돌염전",
                "수산봉", "항파두리항몽유적지", "연화지", "카페거리");
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