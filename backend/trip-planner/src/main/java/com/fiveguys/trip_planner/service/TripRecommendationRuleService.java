package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.KakaoPlaceDto;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TripRecommendationRuleService {

    private static final List<String> EXCLUDED_CATEGORY_KEYWORDS = List.of(
            "카페", "커피", "커피전문점",
            "음식점", "레스토랑", "패스트푸드",
            "편의점", "마트", "백화점", "쇼핑", "판매점", "대리점",
            "호텔", "숙박", "모텔", "게스트하우스",
            "병원", "약국", "부동산", "학원", "주유소",
            "교차로", "도로시설", "교통", "수송",
            "학교", "대학교", "캠퍼스", "유치원", "초등학교", "중학교", "고등학교",
            "공방", "작업실", "연구소", "주차장", "톨게이트", "ic", "jc", "휴게소",
            "행정", "주민센터", "복지", "터미널", "정류장",
            "서비스", "산업", "공간대여", "의류판매", "전문대행",
            "공원시설물", "시설물",
            "단체", "협회", "사회,공공기관", "공공기관"
    );

    private static final List<String> EXCLUDED_PLACE_NAME_KEYWORDS = List.of(
            "스타벅스", "투썸", "이디야", "메가커피", "컴포즈커피",
            "빽다방", "할리스", "폴바셋", "파스쿠찌",
            "네거리", "교차로", "공방", "활력소",
            "ic", "jc", "톨게이트", "휴게소",
            "초등학교", "중학교", "고등학교", "대학교", "캠퍼스",
            "주민센터", "행정복지센터", "연구소", "작업실", "지원센터",
            "시청", "구청", "군청", "도청", "청사",
            "역광장", "광장갤러리", "전시관", "기념품점",
            "협회", "지원관", "활력소"
    );

    private static final List<String> EXCLUDED_TRAIL_KEYWORDS = List.of(
            "코스", "둘레길", "탐방로", "산책로", "해파랑길", "남파랑길"
    );

    private static final List<String> POSITIVE_CATEGORY_STRONG = List.of(
            "관광명소", "해수욕장", "고궁"
    );

    private static final List<String> POSITIVE_CATEGORY_MEDIUM = List.of(
            "공원", "박물관", "미술관", "문화마을"
    );

    private static final List<String> POSITIVE_CATEGORY_LIGHT = List.of(
            "문화유적", "유적", "마을", "폭포", "타워"
    );

    private static final List<String> POSITIVE_NAME_STRONG = List.of(
            "해수욕장", "문화마을", "한옥마을", "경복궁", "창덕궁",
            "해운대", "광안리", "감천", "흰여울", "영일대",
            "청계천", "북촌"
    );

    private static final List<String> POSITIVE_NAME_LIGHT = List.of(
            "공원", "폭포", "궁", "타워"
    );

    private static final List<String> REPRESENTATIVE_BONUS_KEYWORDS = List.of(
            "문화마을", "해수욕장", "궁", "한옥마을",
            "해운대", "광안리", "감천", "흰여울", "영일대",
            "청계천", "북촌"
    );

    private static final List<String> GENERIC_TRAVEL_KEYWORDS = List.of(
            "관광", "명소", "여행"
    );

    private static final List<String> DERIVED_SPOT_PENALTY_KEYWORDS = List.of(
            "전망대", "하늘마루", "정상", "전망 포인트", "전망쉼터", "포토존", "스카이워크",
            "광장", "전시관"
    );

    private static final List<String> STRONG_LANDMARK_NAME_KEYWORDS = List.of(
            "궁", "문화마을", "한옥마을", "해수욕장", "공원",
            "해운대", "광안리", "감천", "흰여울", "영일대",
            "청계천", "북촌"
    );

    private static final List<String> GOOD_PLAZA_KEYWORDS = List.of(
            "광화문광장", "이순신광장", "biff광장"
    );

    private static final List<String> LOW_PRIORITY_HISTORIC_KEYWORDS = List.of(
            "유적", "알터", "고분", "유물", "사지", "터"
    );

    private static final Map<String, Integer> CATEGORY_MAX_LIMIT = Map.of(
            "BEACH", 2,
            "MUSEUM", 1,
            "PARK", 2,
            "OBSERVATORY", 1,
            "VILLAGE", 2,
            "LANDMARK", 2,
            "ETC", 1
    );

    public boolean isExcludedPlace(KakaoPlaceDto place) {
        String placeName = normalize(place.getPlaceName());
        String categoryName = normalize(place.getCategoryName());
        String addressName = normalize(place.getAddressName());
        String roadAddressName = normalize(place.getRoadAddressName());

        if (containsAny(categoryName, EXCLUDED_CATEGORY_KEYWORDS)) {
            return true;
        }

        if (containsAny(placeName, EXCLUDED_PLACE_NAME_KEYWORDS)) {
            return true;
        }

        if (containsAny(placeName, EXCLUDED_TRAIL_KEYWORDS)) {
            return true;
        }

        if (addressName.contains("산업단지") || roadAddressName.contains("산업단지")) {
            return true;
        }

        if (addressName.contains("교차로") || roadAddressName.contains("교차로")) {
            return true;
        }

        if (placeName.contains("박물관")
                && !categoryName.contains("박물관")
                && !categoryName.contains("문화시설")
                && !categoryName.contains("관광")) {
            return true;
        }

        if (placeName.contains("미술관")
                && !categoryName.contains("미술관")
                && !categoryName.contains("문화시설")
                && !categoryName.contains("관광")) {
            return true;
        }

        if (placeName.contains("광장") && !containsAny(placeName, GOOD_PLAZA_KEYWORDS)) {
            return true;
        }

        if (placeName.contains("전시관") && !categoryName.contains("관광")) {
            return true;
        }

        if (placeName.contains("협회") || placeName.contains("단체")) {
            return true;
        }

        return false;
    }

    public int scorePlace(KakaoPlaceDto place) {
        String placeName = normalize(place.getPlaceName());
        String categoryName = normalize(place.getCategoryName());

        int score = 0;

        score += scoreContains(categoryName, 7, POSITIVE_CATEGORY_STRONG);
        score += scoreContains(categoryName, 3, POSITIVE_CATEGORY_MEDIUM);
        score += scoreContains(categoryName, 1, POSITIVE_CATEGORY_LIGHT);

        score += scoreContains(placeName, 6, POSITIVE_NAME_STRONG);
        score += scoreContains(placeName, 2, POSITIVE_NAME_LIGHT);

        if (containsAny(placeName, REPRESENTATIVE_BONUS_KEYWORDS)) {
            score += 4;
        }

        if (containsAny(categoryName, GENERIC_TRAVEL_KEYWORDS)) {
            score += 2;
        }

        if (containsAny(placeName, DERIVED_SPOT_PENALTY_KEYWORDS)) {
            score -= 5;
        }

        if (isObservatory(place) && !containsAny(placeName, STRONG_LANDMARK_NAME_KEYWORDS)) {
            score -= 3;
        }

        if (isStrongRepresentativePlace(place)) {
            score += 4;
        }

        if (isMuseum(place)) {
            score -= 1;
        }

        if (isGoodPlaza(place)) {
            score += 2;
        }

        if (containsAny(placeName, LOW_PRIORITY_HISTORIC_KEYWORDS)
                && !containsAny(placeName, REPRESENTATIVE_BONUS_KEYWORDS)
                && !placeName.contains("궁")) {
            score -= 3;
        }

        if (categoryName.contains("체험여행") || categoryName.contains("정보화")) {
            score -= 3;
        }

        return score;
    }

    public String classifyCategory(KakaoPlaceDto place) {
        String categoryName = normalize(place.getCategoryName());
        String placeName = normalize(place.getPlaceName());

        if (categoryName.contains("해수욕장") || placeName.contains("해수욕장")) {
            return "BEACH";
        }
        if (categoryName.contains("박물관") || categoryName.contains("미술관")) {
            return "MUSEUM";
        }
        if (categoryName.contains("공원") || categoryName.contains("수목원") || placeName.contains("공원")) {
            return "PARK";
        }
        if (categoryName.contains("전망대") || categoryName.contains("타워") || placeName.contains("전망대")) {
            return "OBSERVATORY";
        }
        if (categoryName.contains("문화마을") || placeName.contains("문화마을")) {
            return "VILLAGE";
        }
        if (categoryName.contains("관광명소")
                || categoryName.contains("고궁")
                || categoryName.contains("문화유적")
                || categoryName.contains("유적")
                || placeName.contains("궁")
                || isGoodPlaza(place)) {
            return "LANDMARK";
        }
        return "ETC";
    }

    public boolean isSimilarPlace(KakaoPlaceDto a, KakaoPlaceDto b) {
        if (StringUtils.hasText(a.getPlaceId()) && StringUtils.hasText(b.getPlaceId())
                && a.getPlaceId().equals(b.getPlaceId())) {
            return true;
        }

        String aName = normalizePlaceName(a.getPlaceName());
        String bName = normalizePlaceName(b.getPlaceName());

        if (!StringUtils.hasText(aName) || !StringUtils.hasText(bName)) {
            return false;
        }

        if (aName.equals(bName)) {
            return true;
        }

        if ((aName.contains(bName) || bName.contains(aName)) && isSameArea(a, b)) {
            return true;
        }

        return shareCoreToken(aName, bName) && isSameArea(a, b);
    }

    public String buildNormalizedUniqueKey(KakaoPlaceDto place) {
        String normalizedName = normalizePlaceName(place.getPlaceName());
        String normalizedAddress = normalizeAddress(place.getAddressName());

        if (StringUtils.hasText(normalizedName) && StringUtils.hasText(normalizedAddress)) {
            return normalizedName + "|" + normalizedAddress;
        }

        return buildUniqueKey(place);
    }

    public String buildUniqueKey(KakaoPlaceDto place) {
        if (StringUtils.hasText(place.getPlaceId())) {
            return place.getPlaceId();
        }
        return (place.getPlaceName() + "|" + place.getAddressName()).trim();
    }

    public int getCategoryMaxLimit(String categoryType) {
        return CATEGORY_MAX_LIMIT.getOrDefault(categoryType, 1);
    }

    public int getRelaxedCategoryLimit(String categoryType) {
        if ("OBSERVATORY".equals(categoryType)) {
            return 1;
        }
        if ("MUSEUM".equals(categoryType)) {
            return 1;
        }
        if ("LANDMARK".equals(categoryType)) {
            return 3;
        }
        if ("PARK".equals(categoryType) || "VILLAGE".equals(categoryType) || "BEACH".equals(categoryType)) {
            return getCategoryMaxLimit(categoryType) + 1;
        }
        return getCategoryMaxLimit(categoryType);
    }

    public boolean canSelectCategory(String categoryType, Map<String, Integer> selectedCountByCategory) {
        int currentCount = selectedCountByCategory.getOrDefault(categoryType, 0);
        return currentCount < getCategoryMaxLimit(categoryType);
    }

    public boolean canSelectCategoryRelaxed(String categoryType, Map<String, Integer> selectedCountByCategory) {
        int currentCount = selectedCountByCategory.getOrDefault(categoryType, 0);
        return currentCount < getRelaxedCategoryLimit(categoryType);
    }

    private boolean isStrongRepresentativePlace(KakaoPlaceDto place) {
        String placeName = normalize(place.getPlaceName());
        String categoryName = normalize(place.getCategoryName());

        return containsAny(placeName, STRONG_LANDMARK_NAME_KEYWORDS)
                || categoryName.contains("고궁")
                || categoryName.contains("관광명소");
    }

    private boolean isObservatory(KakaoPlaceDto place) {
        String placeName = normalize(place.getPlaceName());
        String categoryName = normalize(place.getCategoryName());

        return categoryName.contains("전망대")
                || categoryName.contains("타워")
                || placeName.contains("전망대")
                || placeName.contains("타워");
    }

    private boolean isMuseum(KakaoPlaceDto place) {
        String placeName = normalize(place.getPlaceName());
        String categoryName = normalize(place.getCategoryName());

        return categoryName.contains("박물관")
                || categoryName.contains("미술관")
                || placeName.contains("박물관")
                || placeName.contains("미술관");
    }

    private boolean isGoodPlaza(KakaoPlaceDto place) {
        String placeName = normalize(place.getPlaceName());
        return containsAny(placeName, GOOD_PLAZA_KEYWORDS);
    }

    private boolean isSameArea(KakaoPlaceDto a, KakaoPlaceDto b) {
        String aRoad = normalizeAddress(a.getRoadAddressName());
        String bRoad = normalizeAddress(b.getRoadAddressName());

        if (StringUtils.hasText(aRoad) && aRoad.equals(bRoad)) {
            return true;
        }

        String aAddress = normalizeAddress(a.getAddressName());
        String bAddress = normalizeAddress(b.getAddressName());

        if (StringUtils.hasText(aAddress) && aAddress.equals(bAddress)) {
            return true;
        }

        return isNearby(a, b);
    }

    private boolean isNearby(KakaoPlaceDto a, KakaoPlaceDto b) {
        try {
            if (!StringUtils.hasText(a.getLongitude()) || !StringUtils.hasText(a.getLatitude())
                    || !StringUtils.hasText(b.getLongitude()) || !StringUtils.hasText(b.getLatitude())) {
                return false;
            }

            double ax = Double.parseDouble(a.getLongitude());
            double ay = Double.parseDouble(a.getLatitude());
            double bx = Double.parseDouble(b.getLongitude());
            double by = Double.parseDouble(b.getLatitude());

            double diff = Math.abs(ax - bx) + Math.abs(ay - by);
            return diff < 0.01;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean shareCoreToken(String aName, String bName) {
        List<String> aTokens = splitCoreTokens(aName);
        List<String> bTokens = splitCoreTokens(bName);

        for (String aToken : aTokens) {
            if (aToken.length() < 2) {
                continue;
            }
            for (String bToken : bTokens) {
                if (bToken.length() < 2) {
                    continue;
                }
                if (aToken.equals(bToken)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> splitCoreTokens(String normalizedName) {
        String value = normalizedName
                .replace("전망대", " ")
                .replace("박물관", " ")
                .replace("미술관", " ")
                .replace("공원", " ")
                .replace("해수욕장", " ")
                .replace("문화마을", " ")
                .replace("광장", " ")
                .replace("타워", " ")
                .replace("하늘마루", " ")
                .replace("정상", " ")
                .replace("전망쉼터", " ")
                .replace("전망포인트", " ");

        String[] rawTokens = value.split("\\s+");
        List<String> result = new ArrayList<>();
        for (String token : rawTokens) {
            if (StringUtils.hasText(token)) {
                result.add(token.trim());
            }
        }
        return result;
    }

    private int scoreContains(String target, int weight, List<String> keywords) {
        int total = 0;
        for (String keyword : keywords) {
            if (target.contains(keyword.toLowerCase())) {
                total += weight;
            }
        }
        return total;
    }

    private boolean containsAny(String target, List<String> keywords) {
        for (String keyword : keywords) {
            if (target.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String normalizePlaceName(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.toLowerCase();
        normalized = normalized.replaceAll("[^가-힣a-z0-9\\s]", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        normalized = normalized.replace("본관", "");
        normalized = normalized.replace("분관", "");
        normalized = normalized.replace("직영점", "");
        normalized = normalized.replace("지점", "");
        normalized = normalized.replace("하늘마루", "");
        normalized = normalized.replace("전시관", "");
        normalized = normalized.replace("기념관", "");
        normalized = normalized.replace("전망쉼터", "");
        normalized = normalized.replace("전망포인트", "");

        return normalized.replaceAll("\\s+", " ").trim();
    }

    private String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase()
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}