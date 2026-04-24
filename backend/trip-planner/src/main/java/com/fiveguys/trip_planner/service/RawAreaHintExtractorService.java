package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.List;

@Service
public class RawAreaHintExtractorService {

    private static final List<String> INTENT_STOPWORDS = List.of(
            "맛집", "음식", "음식점", "식당", "밥집", "먹거리",
            "카페", "디저트", "베이커리", "빵집",
            "술집", "주점", "포차", "호프",
            "숙소", "숙박", "호텔", "모텔", "펜션", "리조트", "게스트하우스",
            "명소", "관광지", "볼거리", "랜드마크", "핫플", "핫플레이스",
            "일정", "코스", "여행", "플랜", "동선", "루트",
            "추천", "추천해", "추천해줘",
            "알려", "알려줘",
            "찾아", "찾아줘",
            "해줘", "해줘요", "해주세요",
            "줘", "좀", "좀만"
    );

    private static final List<String> LOCATION_MODIFIERS = List.of(
            "근처", "주변", "쪽", "근방", "부근", "일대", "인근"
    );

    private static final List<String> GENERIC_AREA_WORDS = List.of(
            "공항", "역", "터미널", "버스터미널", "기차역"
    );

    public String extract(String message, String city) {
        String normalizedMessage = normalize(message);
        String normalizedCity = normalize(city);

        if (!StringUtils.hasText(normalizedMessage)) {
            return "";
        }

        String withoutCity = removeCityExpression(normalizedMessage, normalizedCity);
        String cleaned = removeNoiseWords(withoutCity);

        String hint = extractModifierBasedHint(cleaned);
        if (StringUtils.hasText(hint)) {
            return hint;
        }

        return "";
    }

    private String extractModifierBasedHint(String value) {
        String[] tokens = value.split("\\s+");

        for (int i = 0; i < tokens.length; i++) {
            String token = stripParticle(tokens[i]);

            for (String modifier : LOCATION_MODIFIERS) {
                if (token.endsWith(modifier)) {
                    String candidate = token.substring(0, token.length() - modifier.length());
                    candidate = stripParticle(candidate);

                    if (isValidHint(candidate)) {
                        return candidate;
                    }
                }

                if (token.equals(modifier) && i > 0) {
                    String candidate = stripParticle(tokens[i - 1]);

                    if (isValidHint(candidate)) {
                        return candidate;
                    }
                }
            }
        }

        return "";
    }

    private String removeCityExpression(String message, String city) {
        if (!StringUtils.hasText(city)) {
            return message;
        }

        String result = message.replace(city, " ");

        String strippedCity = stripRegionSuffix(city);
        if (StringUtils.hasText(strippedCity) && strippedCity.length() >= 2) {
            result = result.replace(strippedCity, " ");
        }

        return result.replaceAll("\\s+", " ").trim();
    }

    private String removeNoiseWords(String value) {
        String result = value;

        for (String stopword : INTENT_STOPWORDS) {
            result = result.replace(stopword, " ");
        }

        result = result.replaceAll("(가볼\\s*만한\\s*곳|갈\\s*만한\\s*곳|놀러갈\\s*만한\\s*곳|구경할\\s*만한\\s*곳)", " ");
        result = result.replaceAll("(여행가는데|여행가려는데|놀러가는데)", " ");

        return result.replaceAll("\\s+", " ").trim();
    }

    private boolean isValidHint(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String normalized = stripParticle(value);

        if (normalized.length() < 2) {
            return false;
        }

        if (isStopword(normalized)) {
            return false;
        }

        if (GENERIC_AREA_WORDS.contains(normalized)) {
            return false;
        }

        return normalized.matches(".*[가-힣a-z0-9].*");
    }

    private boolean isStopword(String value) {
        return INTENT_STOPWORDS.contains(value)
                || LOCATION_MODIFIERS.contains(value);
    }

    private String stripParticle(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value
                .replaceAll("(에서|으로|로|은|는|이|가|을|를|에|의|도|만)$", "")
                .trim();
    }

    private String stripRegionSuffix(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.replaceAll("(특별자치도|특별자치시|광역시|특별시|도|시|군|구)$", "").trim();
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