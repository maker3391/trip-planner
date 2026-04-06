package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.DayPlanDraft;
import com.fiveguys.trip_planner.dto.RecommendationDraft;
import com.fiveguys.trip_planner.dto.RecommendationItemDraft;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RecommendationNormalizationService {

    private static final int MAX_PLACES_PER_DAY = 4;
    private static final int MAX_ITEMS = 5;

    private static final Map<String, String> DESTINATION_ALIASES = new LinkedHashMap<>();

    private final DetailAreaParsingService detailAreaParsingService;

    static {
        putAliases("서울",
                "서울", "서울시", "서울특별시",
                "seoul", "seoul city", "seoul special city", "seoul, south korea");

        putAliases("부산",
                "부산", "부산시", "부산광역시",
                "busan", "busan city", "busan metropolitan city", "busan, south korea");

        putAliases("대구",
                "대구", "대구시", "대구광역시",
                "daegu", "daegu city", "daegu metropolitan city", "daegu, south korea");

        putAliases("인천",
                "인천", "인천시", "인천광역시",
                "incheon", "incheon city", "incheon metropolitan city", "incheon, south korea");

        putAliases("광주",
                "광주", "광주시", "광주광역시",
                "gwangju", "gwangju city", "gwangju metropolitan city", "gwangju, south korea");

        putAliases("대전",
                "대전", "대전시", "대전광역시",
                "daejeon", "daejeon city", "daejeon metropolitan city", "daejeon, south korea");

        putAliases("울산",
                "울산", "울산시", "울산광역시",
                "ulsan", "ulsan city", "ulsan metropolitan city", "ulsan, south korea");

        putAliases("세종",
                "세종", "세종시", "세종특별자치시",
                "sejong", "sejong city", "sejong special self-governing city", "sejong, south korea");

        putAliases("제주",
                "제주", "제주시", "제주도", "제주특별자치도",
                "jeju", "jeju city", "jeju island", "jeju-do", "jeju, south korea");

        putAliases("경기",
                "경기", "경기도",
                "gyeonggi", "gyeonggi-do", "gyeonggi province");

        putAliases("강원",
                "강원", "강원도", "강원특별자치도",
                "gangwon", "gangwon-do", "gangwon state", "gangwon special self-governing province");

        putAliases("충북",
                "충북", "충청북도",
                "chungbuk", "chungcheongbuk-do", "north chungcheong");

        putAliases("충남",
                "충남", "충청남도", "충청도",
                "chungnam", "chungcheongnam-do", "south chungcheong");

        putAliases("전북",
                "전북", "전라북도", "전북특별자치도",
                "jeonbuk", "jeollabuk-do", "north jeolla");

        putAliases("전남",
                "전남", "전라남도", "전라도",
                "jeonnam", "jeollanam-do", "south jeolla");

        putAliases("경북",
                "경북", "경상북도",
                "gyeongbuk", "gyeongsangbuk-do", "north gyeongsang");

        putAliases("경남",
                "경남", "경상남도", "경상도",
                "gyeongnam", "gyeongsangnam-do", "south gyeongsang");

        putAliases("여수",
                "여수", "여수시",
                "yeosu", "yeosu-si", "yeosu city");

        putAliases("순천",
                "순천", "순천시",
                "suncheon", "suncheon-si", "suncheon city");

        putAliases("목포",
                "목포", "목포시",
                "mokpo", "mokpo-si", "mokpo city");

        putAliases("전주",
                "전주", "전주시",
                "jeonju", "jeonju-si", "jeonju city");

        putAliases("군산",
                "군산", "군산시",
                "gunsan", "gunsan-si", "gunsan city");

        putAliases("강릉",
                "강릉", "강릉시",
                "gangneung", "gangneung-si", "gangneung city");

        putAliases("속초",
                "속초", "속초시",
                "sokcho", "sokcho-si", "sokcho city");

        putAliases("춘천",
                "춘천", "춘천시",
                "chuncheon", "chuncheon-si", "chuncheon city");

        putAliases("경주",
                "경주", "경주시",
                "gyeongju", "gyeongju-si", "gyeongju city");

        putAliases("포항",
                "포항", "포항시",
                "pohang", "pohang-si", "pohang city");

        putAliases("안동",
                "안동", "안동시",
                "andong", "andong-si", "andong city");

        putAliases("창원",
                "창원", "창원시",
                "changwon", "changwon-si", "changwon city");

        putAliases("통영",
                "통영", "통영시",
                "tongyeong", "tongyeong-si", "tongyeong city");

        putAliases("거제",
                "거제", "거제시",
                "geoje", "geoje-si", "geoje city");

        putAliases("진주",
                "진주", "진주시",
                "jinju", "jinju-si", "jinju city");

        putAliases("가평",
                "가평", "가평군",
                "gapyeong", "gapyeong-gun");

        putAliases("수원",
                "수원", "수원시",
                "suwon", "suwon-si");

        putAliases("고양",
                "고양", "고양시",
                "goyang", "goyang-si");

        putAliases("파주",
                "파주", "파주시",
                "paju", "paju-si");

        putAliases("강화",
                "강화", "강화군",
                "ganghwa", "ganghwa-gun");
    }

    public RecommendationNormalizationService(DetailAreaParsingService detailAreaParsingService) {
        this.detailAreaParsingService = detailAreaParsingService;
    }

    public RecommendationDraft normalize(RecommendationDraft draft) {
        RecommendationDraft normalized = new RecommendationDraft();
        normalized.setIntent(normalizeIntent(draft.getIntent()));

        String normalizedDetailArea = normalizeDetailArea(draft);
        String normalizedDestination = normalizeDestination(draft.getDestination(), normalizedDetailArea);

        normalized.setDestination(normalizedDestination);
        normalized.setDetailArea(normalizedDetailArea);
        normalized.setDays(normalizeDays(draft.getDays(), normalized.getIntent()));

        if ("TRAVEL_ITINERARY".equals(normalized.getIntent())) {
            normalized.setDayPlans(normalizeDayPlans(draft.getDayPlans(), normalized.getDays()));
            normalized.setItems(new ArrayList<>());
        } else {
            normalized.setDayPlans(new ArrayList<>());
            normalized.setItems(normalizeItems(draft.getItems()));
            normalized.setDays(null);
        }

        return normalized;
    }

    private String normalizeIntent(String intent) {
        String value = trimToNull(intent);
        if (value == null) {
            return null;
        }

        String normalized = normalizeText(value);

        if (normalized.contains("travel") || normalized.contains("itinerary") || normalized.contains("trip")) {
            return "TRAVEL_ITINERARY";
        }

        if (normalized.contains("restaurant") || normalized.contains("food")) {
            return "RESTAURANT_RECOMMENDATION";
        }

        if (normalized.contains("stay") || normalized.contains("hotel") || normalized.contains("accommodation")) {
            return "STAY_RECOMMENDATION";
        }

        return value.trim();
    }

    private Integer normalizeDays(Integer days, String intent) {
        if (!"TRAVEL_ITINERARY".equals(intent)) {
            return null;
        }

        if (days == null || days < 1) {
            return null;
        }

        if (days > 10) {
            return 10;
        }

        return days;
    }

    private List<DayPlanDraft> normalizeDayPlans(List<DayPlanDraft> dayPlans, Integer days) {
        List<DayPlanDraft> result = new ArrayList<>();
        if (dayPlans == null || dayPlans.isEmpty() || days == null || days < 1) {
            return result;
        }

        Set<String> globalPlaceSignatures = new LinkedHashSet<>();

        for (int i = 0; i < dayPlans.size() && i < days; i++) {
            DayPlanDraft source = dayPlans.get(i);
            DayPlanDraft target = new DayPlanDraft();
            target.setDay(i + 1);
            target.setPlaces(normalizePlaces(source == null ? null : source.getPlaces(), globalPlaceSignatures));
            result.add(target);
        }

        return result;
    }

    private List<String> normalizePlaces(List<String> places, Set<String> globalPlaceSignatures) {
        List<String> result = new ArrayList<>();
        if (places == null || places.isEmpty()) {
            return result;
        }

        Set<String> localSignatures = new LinkedHashSet<>();

        for (String place : places) {
            String normalizedPlace = normalizePlaceName(place);
            if (normalizedPlace == null) {
                continue;
            }

            String signature = buildLooseSignature(normalizedPlace);

            if (localSignatures.contains(signature)) {
                continue;
            }

            if (globalPlaceSignatures.contains(signature)) {
                continue;
            }

            localSignatures.add(signature);
            globalPlaceSignatures.add(signature);
            result.add(normalizedPlace);

            if (result.size() >= MAX_PLACES_PER_DAY) {
                break;
            }
        }

        return result;
    }

    private List<RecommendationItemDraft> normalizeItems(List<RecommendationItemDraft> items) {
        List<RecommendationItemDraft> result = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return result;
        }

        Set<String> dedupNames = new LinkedHashSet<>();

        for (RecommendationItemDraft item : items) {
            if (item == null) {
                continue;
            }

            String name = normalizePlaceName(item.getName());
            if (name == null) {
                continue;
            }

            String signature = buildLooseSignature(name);
            if (dedupNames.contains(signature)) {
                continue;
            }

            result.add(new RecommendationItemDraft(name));
            dedupNames.add(signature);

            if (result.size() >= MAX_ITEMS) {
                break;
            }
        }

        return result;
    }

    private String normalizeDestination(String destination, String normalizedDetailArea) {
        if (StringUtils.hasText(normalizedDetailArea)) {
            String parentCity = detailAreaParsingService.resolveParentCity(normalizedDetailArea);
            if (StringUtils.hasText(parentCity)) {
                return parentCity;
            }
        }

        String value = trimToNull(destination);
        if (value == null) {
            return null;
        }

        String normalized = normalizeText(value);

        String matched = DESTINATION_ALIASES.get(normalized);
        if (matched != null) {
            return matched;
        }

        String compact = normalized.replace(" ", "");
        matched = DESTINATION_ALIASES.get(compact);
        if (matched != null) {
            return matched;
        }

        String stripped = stripAdministrativeSuffix(value);
        if (StringUtils.hasText(stripped)) {
            String strippedNormalized = normalizeText(stripped);
            matched = DESTINATION_ALIASES.get(strippedNormalized);
            if (matched != null) {
                return matched;
            }

            return stripped.trim();
        }

        return value.trim();
    }

    private String normalizeDetailArea(RecommendationDraft draft) {
        String detailArea = trimToNull(draft.getDetailArea());
        if (detailArea != null) {
            String extracted = detailAreaParsingService.extractDetailArea(detailArea);
            if (StringUtils.hasText(extracted)) {
                return extracted;
            }
        }

        String destination = trimToNull(draft.getDestination());
        if (destination != null) {
            String extracted = detailAreaParsingService.extractDetailArea(destination);
            if (StringUtils.hasText(extracted)) {
                return extracted;
            }
        }

        return null;
    }

    private String stripAdministrativeSuffix(String value) {
        String trimmed = value.trim();

        String[] suffixes = {
                "특별자치시", "특별자치도", "특별시", "광역시",
                "시", "도", "군", "구"
        };

        for (String suffix : suffixes) {
            if (trimmed.endsWith(suffix) && trimmed.length() > suffix.length()) {
                return trimmed.substring(0, trimmed.length() - suffix.length());
            }
        }

        return trimmed;
    }

    private String normalizePlaceName(String value) {
        String text = trimToNull(value);
        if (text == null) {
            return null;
        }

        String normalized = text
                .replaceAll("\\s+", " ")
                .replaceAll("^[0-9]+[.)]\\s*", "")
                .replaceAll("^(추천|방문|일정|코스)\\s*[:：-]?\\s*", "")
                .replaceAll("\\s*[-–—]\\s*추천$", "")
                .replaceAll("\\s*[-–—]\\s*방문$", "")
                .trim();

        if (!StringUtils.hasText(normalized)) {
            return null;
        }

        if (normalized.length() > 40) {
            return null;
        }

        if (normalized.endsWith("여행") || normalized.endsWith("일정") || normalized.endsWith("코스")) {
            return null;
        }

        return normalized;
    }

    private String buildLooseSignature(String value) {
        return normalizeText(value)
                .replaceAll("[\\s\\-_/()\\[\\],.·]", "");
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase()
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static void putAliases(String canonical, String... aliases) {
        for (String alias : aliases) {
            DESTINATION_ALIASES.put(normalizeStatic(alias), canonical);
        }
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