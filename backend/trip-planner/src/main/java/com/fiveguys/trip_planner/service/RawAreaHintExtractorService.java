package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class RawAreaHintExtractorService {

    private static final Pattern NOISE_PATTERN = Pattern.compile(
            "(맛집|음식|식당|카페|먹거리|술집|밥집|추천|숙소|호텔|리조트|펜션|게스트하우스|모텔|호스텔|민박|풀빌라|한옥스테이|에어비앤비|여행|일정|코스)"
    );

    public String extract(String message, String city) {
        if (!StringUtils.hasText(message)) {
            return "";
        }

        String normalizedMessage = normalize(message);
        String normalizedCity = normalize(city);

        String[] tokens = normalizedMessage.split("\\s+");
        for (String token : tokens) {
            if (!StringUtils.hasText(token)) {
                continue;
            }

            if (NOISE_PATTERN.matcher(token).find()) {
                continue;
            }

            if (StringUtils.hasText(normalizedCity) && token.equals(normalizedCity)) {
                continue;
            }

            if (token.length() < 2) {
                continue;
            }

            return token;
        }

        return "";
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}