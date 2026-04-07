package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.ParsedRegion;
import com.fiveguys.trip_planner.dto.RegionScope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class RegionParsingService {

    private static final Map<String, String> PROVINCE_ALIASES = new LinkedHashMap<>();
    private static final Map<String, String> LOCAL_GOV_ALIASES = new LinkedHashMap<>();
    private static final Map<String, String> LOCAL_GOV_TO_PROVINCE = new LinkedHashMap<>();
    private static final Set<String> DISTRICT_ALIASES = new LinkedHashSet<>();

    static {
        registerProvince("서울", "서울", "서울시", "서울특별시");
        registerProvince("부산", "부산", "부산시", "부산광역시");
        registerProvince("대구", "대구", "대구시", "대구광역시");
        registerProvince("인천", "인천", "인천시", "인천광역시");
        registerProvince("광주", "광주", "광주시", "광주광역시");
        registerProvince("대전", "대전", "대전시", "대전광역시");
        registerProvince("울산", "울산", "울산시", "울산광역시");
        registerProvince("세종", "세종", "세종시", "세종특별자치시");
        registerProvince("경기", "경기", "경기도");
        registerProvince("강원", "강원", "강원도", "강원특별자치도");
        registerProvince("충북", "충북", "충청북도");
        registerProvince("충남", "충남", "충청남도");
        registerProvince("전북", "전북", "전라북도", "전북특별자치도");
        registerProvince("전남", "전남", "전라남도");
        registerProvince("경북", "경북", "경상북도");
        registerProvince("경남", "경남", "경상남도");
        registerProvince("제주", "제주", "제주도", "제주특별자치도");

        registerMetroCity("서울", "서울", "서울시", "서울특별시");
        registerMetroCity("부산", "부산", "부산시", "부산광역시");
        registerMetroCity("대구", "대구", "대구시", "대구광역시");
        registerMetroCity("인천", "인천", "인천시", "인천광역시");
        registerMetroCity("광주", "광주", "광주시", "광주광역시");
        registerMetroCity("대전", "대전", "대전시", "대전광역시");
        registerMetroCity("울산", "울산", "울산시", "울산광역시");
        registerMetroCity("세종", "세종", "세종시", "세종특별자치시");
        registerMetroCity("제주", "제주", "제주도", "제주특별자치도");
        registerMetroCity("서귀포", "서귀포", "서귀포시");

        registerRegionalCity("경기", "수원", "수원", "수원시");
        registerRegionalCity("경기", "성남", "성남", "성남시");
        registerRegionalCity("경기", "의정부", "의정부", "의정부시");
        registerRegionalCity("경기", "안양", "안양", "안양시");
        registerRegionalCity("경기", "부천", "부천", "부천시");
        registerRegionalCity("경기", "광명", "광명", "광명시");
        registerRegionalCity("경기", "평택", "평택", "평택시");
        registerRegionalCity("경기", "동두천", "동두천", "동두천시");
        registerRegionalCity("경기", "안산", "안산", "안산시");
        registerRegionalCity("경기", "고양", "고양", "고양시");
        registerRegionalCity("경기", "과천", "과천", "과천시");
        registerRegionalCity("경기", "구리", "구리", "구리시");
        registerRegionalCity("경기", "남양주", "남양주", "남양주시");
        registerRegionalCity("경기", "오산", "오산", "오산시");
        registerRegionalCity("경기", "시흥", "시흥", "시흥시");
        registerRegionalCity("경기", "군포", "군포", "군포시");
        registerRegionalCity("경기", "의왕", "의왕", "의왕시");
        registerRegionalCity("경기", "하남", "하남", "하남시");
        registerRegionalCity("경기", "용인", "용인", "용인시");
        registerRegionalCity("경기", "파주", "파주", "파주시");
        registerRegionalCity("경기", "이천", "이천", "이천시");
        registerRegionalCity("경기", "안성", "안성", "안성시");
        registerRegionalCity("경기", "김포", "김포", "김포시");
        registerRegionalCity("경기", "화성", "화성", "화성시");
        registerRegionalCity("경기", "광주", "경기광주", "광주시");
        registerRegionalCity("경기", "양주", "양주", "양주시");
        registerRegionalCity("경기", "포천", "포천", "포천시");
        registerRegionalCity("경기", "여주", "여주", "여주시");
        registerRegionalCity("경기", "연천군", "연천", "연천군");
        registerRegionalCity("경기", "가평군", "가평", "가평군");
        registerRegionalCity("경기", "양평군", "양평", "양평군");

        registerRegionalCity("강원", "춘천", "춘천", "춘천시");
        registerRegionalCity("강원", "원주", "원주", "원주시");
        registerRegionalCity("강원", "강릉", "강릉", "강릉시");
        registerRegionalCity("강원", "동해", "동해", "동해시");
        registerRegionalCity("강원", "태백", "태백", "태백시");
        registerRegionalCity("강원", "속초", "속초", "속초시");
        registerRegionalCity("강원", "삼척", "삼척", "삼척시");
        registerRegionalCity("강원", "홍천군", "홍천", "홍천군");
        registerRegionalCity("강원", "횡성군", "횡성", "횡성군");
        registerRegionalCity("강원", "영월군", "영월", "영월군");
        registerRegionalCity("강원", "평창군", "평창", "평창군");
        registerRegionalCity("강원", "정선군", "정선", "정선군");
        registerRegionalCity("강원", "철원군", "철원", "철원군");
        registerRegionalCity("강원", "화천군", "화천", "화천군");
        registerRegionalCity("강원", "양구군", "양구", "양구군");
        registerRegionalCity("강원", "인제군", "인제", "인제군");
        registerRegionalCity("강원", "고성군", "고성", "고성군");
        registerRegionalCity("강원", "양양군", "양양", "양양군");

        registerRegionalCity("충북", "청주", "청주", "청주시");
        registerRegionalCity("충북", "충주", "충주", "충주시");
        registerRegionalCity("충북", "제천", "제천", "제천시");
        registerRegionalCity("충북", "보은군", "보은", "보은군");
        registerRegionalCity("충북", "옥천군", "옥천", "옥천군");
        registerRegionalCity("충북", "영동군", "영동", "영동군");
        registerRegionalCity("충북", "증평군", "증평", "증평군");
        registerRegionalCity("충북", "진천군", "진천", "진천군");
        registerRegionalCity("충북", "괴산군", "괴산", "괴산군");
        registerRegionalCity("충북", "음성군", "음성", "음성군");
        registerRegionalCity("충북", "단양군", "단양", "단양군");

        registerRegionalCity("충남", "천안", "천안", "천안시");
        registerRegionalCity("충남", "공주", "공주", "공주시");
        registerRegionalCity("충남", "보령", "보령", "보령시");
        registerRegionalCity("충남", "아산", "아산", "아산시");
        registerRegionalCity("충남", "서산", "서산", "서산시");
        registerRegionalCity("충남", "논산", "논산", "논산시");
        registerRegionalCity("충남", "계룡", "계룡", "계룡시");
        registerRegionalCity("충남", "당진", "당진", "당진시");
        registerRegionalCity("충남", "금산군", "금산", "금산군");
        registerRegionalCity("충남", "부여군", "부여", "부여군");
        registerRegionalCity("충남", "서천군", "서천", "서천군");
        registerRegionalCity("충남", "청양군", "청양", "청양군");
        registerRegionalCity("충남", "홍성군", "홍성", "홍성군");
        registerRegionalCity("충남", "예산군", "예산", "예산군");
        registerRegionalCity("충남", "태안군", "태안", "태안군");

        registerRegionalCity("전북", "전주", "전주", "전주시");
        registerRegionalCity("전북", "군산", "군산", "군산시");
        registerRegionalCity("전북", "익산", "익산", "익산시");
        registerRegionalCity("전북", "정읍", "정읍", "정읍시");
        registerRegionalCity("전북", "남원", "남원", "남원시");
        registerRegionalCity("전북", "김제", "김제", "김제시");
        registerRegionalCity("전북", "완주군", "완주", "완주군");
        registerRegionalCity("전북", "진안군", "진안", "진안군");
        registerRegionalCity("전북", "무주군", "무주", "무주군");
        registerRegionalCity("전북", "장수군", "장수", "장수군");
        registerRegionalCity("전북", "임실군", "임실", "임실군");
        registerRegionalCity("전북", "순창군", "순창", "순창군");
        registerRegionalCity("전북", "고창군", "고창", "고창군");
        registerRegionalCity("전북", "부안군", "부안", "부안군");

        registerRegionalCity("전남", "목포", "목포", "목포시");
        registerRegionalCity("전남", "여수", "여수", "여수시");
        registerRegionalCity("전남", "순천", "순천", "순천시");
        registerRegionalCity("전남", "나주", "나주", "나주시");
        registerRegionalCity("전남", "광양", "광양", "광양시");
        registerRegionalCity("전남", "담양군", "담양", "담양군");
        registerRegionalCity("전남", "곡성군", "곡성", "곡성군");
        registerRegionalCity("전남", "구례군", "구례", "구례군");
        registerRegionalCity("전남", "고흥군", "고흥", "고흥군");
        registerRegionalCity("전남", "보성군", "보성", "보성군");
        registerRegionalCity("전남", "화순군", "화순", "화순군");
        registerRegionalCity("전남", "장흥군", "장흥", "장흥군");
        registerRegionalCity("전남", "강진군", "강진", "강진군");
        registerRegionalCity("전남", "해남군", "해남", "해남군");
        registerRegionalCity("전남", "영암군", "영암", "영암군");
        registerRegionalCity("전남", "무안군", "무안", "무안군");
        registerRegionalCity("전남", "함평군", "함평", "함평군");
        registerRegionalCity("전남", "영광군", "영광", "영광군");
        registerRegionalCity("전남", "장성군", "장성", "장성군");
        registerRegionalCity("전남", "완도군", "완도", "완도군");
        registerRegionalCity("전남", "진도군", "진도", "진도군");
        registerRegionalCity("전남", "신안군", "신안", "신안군");

        registerRegionalCity("경북", "포항", "포항", "포항시");
        registerRegionalCity("경북", "경주", "경주", "경주시");
        registerRegionalCity("경북", "김천", "김천", "김천시");
        registerRegionalCity("경북", "안동", "안동", "안동시");
        registerRegionalCity("경북", "구미", "구미", "구미시");
        registerRegionalCity("경북", "영주", "영주", "영주시");
        registerRegionalCity("경북", "영천", "영천", "영천시");
        registerRegionalCity("경북", "상주", "상주", "상주시");
        registerRegionalCity("경북", "문경", "문경", "문경시");
        registerRegionalCity("경북", "경산", "경산", "경산시");
        registerRegionalCity("경북", "의성군", "의성", "의성군");
        registerRegionalCity("경북", "청송군", "청송", "청송군");
        registerRegionalCity("경북", "영양군", "영양", "영양군");
        registerRegionalCity("경북", "영덕군", "영덕", "영덕군");
        registerRegionalCity("경북", "청도군", "청도", "청도군");
        registerRegionalCity("경북", "고령군", "고령", "고령군");
        registerRegionalCity("경북", "성주군", "성주", "성주군");
        registerRegionalCity("경북", "칠곡군", "칠곡", "칠곡군");
        registerRegionalCity("경북", "예천군", "예천", "예천군");
        registerRegionalCity("경북", "봉화군", "봉화", "봉화군");
        registerRegionalCity("경북", "울진군", "울진", "울진군");
        registerRegionalCity("경북", "울릉군", "울릉", "울릉군");

        registerRegionalCity("경남", "창원", "창원", "창원시");
        registerRegionalCity("경남", "진주", "진주", "진주시");
        registerRegionalCity("경남", "통영", "통영", "통영시");
        registerRegionalCity("경남", "사천", "사천", "사천시");
        registerRegionalCity("경남", "김해", "김해", "김해시");
        registerRegionalCity("경남", "밀양", "밀양", "밀양시");
        registerRegionalCity("경남", "거제", "거제", "거제시");
        registerRegionalCity("경남", "양산", "양산", "양산시");
        registerRegionalCity("경남", "의령군", "의령", "의령군");
        registerRegionalCity("경남", "함안군", "함안", "함안군");
        registerRegionalCity("경남", "창녕군", "창녕", "창녕군");
        registerRegionalCity("경남", "고성군", "고성", "고성군");
        registerRegionalCity("경남", "남해군", "남해", "남해군");
        registerRegionalCity("경남", "하동군", "하동", "하동군");
        registerRegionalCity("경남", "산청군", "산청", "산청군");
        registerRegionalCity("경남", "함양군", "함양", "함양군");
        registerRegionalCity("경남", "거창군", "거창", "거창군");
        registerRegionalCity("경남", "합천군", "합천", "합천군");

        registerDistrictAliases(
                "종로구", "중구", "용산구", "성동구", "광진구", "동대문구", "중랑구",
                "성북구", "강북구", "도봉구", "노원구", "은평구", "서대문구", "마포구",
                "양천구", "강서구", "구로구", "금천구", "영등포구", "동작구", "관악구",
                "서초구", "강남구", "송파구", "강동구",
                "서구", "동구", "남구", "북구", "수성구", "달서구",
                "연수구", "미추홀구", "남동구", "부평구", "계양구",
                "유성구", "대덕구",
                "덕양구", "일산동구", "일산서구",
                "권선구", "영통구", "장안구", "팔달구",
                "수정구", "중원구", "분당구",
                "만안구", "동안구",
                "상록구", "단원구",
                "처인구", "기흥구", "수지구",
                "덕진구", "완산구"
        );
    }

    public ParsedRegion parse(String originalMessage, String destination) {
        String merged = normalize(originalMessage + " " + destination);
        String normalizedDestination = normalize(destination);

        String province = extractProvince(merged, normalizedDestination);
        String city = extractLocalGov(merged, normalizedDestination);

        if (!StringUtils.hasText(province) && StringUtils.hasText(city)) {
            province = LOCAL_GOV_TO_PROVINCE.get(city);
        }

        String district = extractDistrict(merged, normalizedDestination, province, city);
        String neighborhood = extractNeighborhood(merged, normalizedDestination);
        RegionScope scope = resolveScope(province, city, district, neighborhood);
        String displayDestination = resolveDisplayDestination(province, city, district, neighborhood, destination);

        return new ParsedRegion(province, city, district, neighborhood, scope, displayDestination);
    }

    private String extractProvince(String merged, String destination) {
        String bestAlias = null;
        String bestProvince = null;

        for (Map.Entry<String, String> entry : PROVINCE_ALIASES.entrySet()) {
            String alias = entry.getKey();
            if (merged.contains(alias) || destination.contains(alias)) {
                if (bestAlias == null || alias.length() > bestAlias.length()) {
                    bestAlias = alias;
                    bestProvince = entry.getValue();
                }
            }
        }

        return bestProvince;
    }

    private String extractLocalGov(String merged, String destination) {
        String bestAlias = null;
        String bestLocalGov = null;

        for (Map.Entry<String, String> entry : LOCAL_GOV_ALIASES.entrySet()) {
            String alias = entry.getKey();
            if (merged.contains(alias) || destination.contains(alias)) {
                if (bestAlias == null || alias.length() > bestAlias.length()) {
                    bestAlias = alias;
                    bestLocalGov = entry.getValue();
                }
            }
        }

        return bestLocalGov;
    }

    private String extractDistrict(String merged,
                                   String destination,
                                   String province,
                                   String localGov) {
        String[] tokens = tokenize(merged + " " + destination);

        return Arrays.stream(tokens)
                .filter(StringUtils::hasText)
                .filter(token -> isDistrictToken(token))
                .filter(token -> !isProvinceToken(token, province))
                .filter(token -> !isLocalGovToken(token, localGov))
                .sorted(Comparator.comparingInt(String::length).reversed())
                .findFirst()
                .orElse(null);
    }

    private String extractNeighborhood(String merged, String destination) {
        String[] tokens = tokenize(merged + " " + destination);

        return Arrays.stream(tokens)
                .filter(StringUtils::hasText)
                .filter(token -> token.endsWith("동") || token.endsWith("읍") || token.endsWith("면") || token.endsWith("리"))
                .sorted(Comparator.comparingInt(String::length).reversed())
                .findFirst()
                .orElse(null);
    }

    private boolean isDistrictToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        String normalizedToken = normalize(token);

        if (DISTRICT_ALIASES.contains(normalizedToken)) {
            return true;
        }

        return normalizedToken.endsWith("구");
    }

    private RegionScope resolveScope(String province, String city, String district, String neighborhood) {
        if (StringUtils.hasText(neighborhood)) {
            return RegionScope.NEIGHBORHOOD;
        }
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
                                             String neighborhood,
                                             String fallbackDestination) {
        if (StringUtils.hasText(neighborhood) && StringUtils.hasText(city)) {
            return city + " " + neighborhood;
        }
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

    private boolean isProvinceToken(String token, String province) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(province)) {
            return false;
        }

        String normalizedToken = normalize(token);
        String normalizedProvince = normalize(province);

        if (normalizedToken.equals(normalizedProvince)) {
            return true;
        }

        return PROVINCE_ALIASES.entrySet().stream()
                .anyMatch(entry ->
                        entry.getValue().equals(province)
                                && normalize(entry.getKey()).equals(normalizedToken)
                );
    }

    private boolean isLocalGovToken(String token, String localGov) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(localGov)) {
            return false;
        }

        String normalizedToken = normalize(token);
        String normalizedLocalGov = normalize(localGov);

        if (normalizedToken.equals(normalizedLocalGov)) {
            return true;
        }

        return LOCAL_GOV_ALIASES.entrySet().stream()
                .anyMatch(entry ->
                        entry.getValue().equals(localGov)
                                && normalize(entry.getKey()).equals(normalizedToken)
                );
    }

    private String[] tokenize(String value) {
        return normalize(value).split("\\s+");
    }

    private static void registerProvince(String canonical, String... aliases) {
        for (String alias : aliases) {
            PROVINCE_ALIASES.put(normalizeStatic(alias), canonical);
        }
    }

    private static void registerMetroCity(String canonical, String... aliases) {
        String province = resolveProvinceForMetro(canonical);
        if (province == null) {
            return;
        }

        for (String alias : aliases) {
            LOCAL_GOV_ALIASES.put(normalizeStatic(alias), canonical);
        }
        LOCAL_GOV_TO_PROVINCE.put(canonical, province);
    }

    private static void registerRegionalCity(String province, String canonical, String... aliases) {
        for (String alias : aliases) {
            LOCAL_GOV_ALIASES.put(normalizeStatic(alias), canonical);
        }
        LOCAL_GOV_TO_PROVINCE.put(canonical, province);
    }

    private static void registerDistrictAliases(String... names) {
        for (String name : names) {
            DISTRICT_ALIASES.add(normalizeStatic(name));
        }
    }

    private static String resolveProvinceForMetro(String canonical) {
        switch (canonical) {
            case "서울":
            case "부산":
            case "대구":
            case "인천":
            case "광주":
            case "대전":
            case "울산":
            case "세종":
            case "제주":
                return canonical;
            case "서귀포":
                return "제주";
            default:
                return null;
        }
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