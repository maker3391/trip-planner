package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AttractionQueryBuilder {

    public List<String> buildQueries(String destination,
                                     String detailArea,
                                     String neighborhood,
                                     String district) {
        return buildQueries(destination, detailArea, neighborhood, district, AttractionSubIntent.GENERAL);
    }

    public List<String> buildQueries(String destination,
                                     String detailArea,
                                     String neighborhood,
                                     String district,
                                     AttractionSubIntent subIntent) {
        Set<String> result = new LinkedHashSet<>();

        List<String> bases = new ArrayList<>();

        if (StringUtils.hasText(detailArea)) {
            bases.add(AttractionTextHelper.joinDistinctLocation(destination, detailArea));
        }

        if (StringUtils.hasText(neighborhood)) {
            bases.add(AttractionTextHelper.joinDistinctLocation(destination, neighborhood));
        }

        if (StringUtils.hasText(district) && !AttractionTextHelper.isCityOrCounty(district)) {
            bases.add(AttractionTextHelper.joinDistinctLocation(destination, district));
        }

        bases.add(destination);

        List<String> expandedBases = new ArrayList<>();

        for (String base : bases) {
            if (!StringUtils.hasText(base)) {
                continue;
            }

            expandedBases.add(base);

            String provinceExpanded = expandProvinceName(base);

            if (StringUtils.hasText(provinceExpanded) && !provinceExpanded.equals(base)) {
                expandedBases.add(provinceExpanded);
            }
        }

        for (String base : expandedBases) {
            addAttractionQueries(result, base, subIntent);
        }

        return new ArrayList<>(result);
    }

    public List<String> buildRelaxedFallbackQueries(String destination, String district) {
        Set<String> result = new LinkedHashSet<>();

        if (StringUtils.hasText(district) && !AttractionTextHelper.isCityOrCounty(district)) {
            addAttractionQueries(result, AttractionTextHelper.joinDistinctLocation(destination, district), AttractionSubIntent.GENERAL);
        }

        addAttractionQueries(result, destination, AttractionSubIntent.GENERAL);

        return new ArrayList<>(result);
    }

    public List<String> buildRelaxedFallbackQueries(String destination,
                                                    String district,
                                                    AttractionSubIntent subIntent) {
        Set<String> result = new LinkedHashSet<>();

        if (StringUtils.hasText(district) && !AttractionTextHelper.isCityOrCounty(district)) {
            addAttractionQueries(result, AttractionTextHelper.joinDistinctLocation(destination, district), subIntent);
        }

        addAttractionQueries(result, destination, subIntent);

        return new ArrayList<>(result);
    }

    private void addAttractionQueries(Set<String> result, String base, AttractionSubIntent subIntent) {
        if (!StringUtils.hasText(base)) {
            return;
        }

        AttractionSubIntent resolvedSubIntent = subIntent == null ? AttractionSubIntent.GENERAL : subIntent;

        switch (resolvedSubIntent) {
            case HOT_PLACE -> {
                result.add(base + " 핫플");
                result.add(base + " 핫플레이스");
                result.add(base + " 인기 명소");
                result.add(base + " 가볼만한곳");
                result.add(base + " 볼거리");
            }
            case DATE_COURSE -> {
                result.add(base + " 데이트코스");
                result.add(base + " 산책로");
                result.add(base + " 야경");
                result.add(base + " 공원");
                result.add(base + " 가볼만한곳");
            }
            case WALK -> {
                result.add(base + " 산책로");
                result.add(base + " 공원");
                result.add(base + " 둘레길");
                result.add(base + " 올레길");
                result.add(base + " 해변");
            }
            case NIGHT_VIEW -> {
                result.add(base + " 야경");
                result.add(base + " 야경 명소");
                result.add(base + " 전망대");
                result.add(base + " 야경 전망대");
                result.add(base + " 대교 야경");
                result.add(base + " 루프탑");
                result.add(base + " 스카이");
                result.add(base + " 랜드마크");

                addNightViewSpecialQueries(result, base);
            }
            case NATURE -> {
                result.add(base + " 자연 명소");
                result.add(base + " 오름");
                result.add(base + " 숲");
                result.add(base + " 해변");
                result.add(base + " 수목원");
                result.add(base + " 휴양림");
            }
            case INDOOR -> {
                result.add(base + " 실내 명소");
                result.add(base + " 실내 놀거리");
                result.add(base + " 아쿠아리움");
                result.add(base + " 수족관");
                result.add(base + " 체험관");
                result.add(base + " 복합문화공간");
                result.add(base + " 문화센터");
                result.add(base + " 공연장");
                result.add(base + " 쇼핑몰");
            }
            case ACTIVITY -> {
                result.add(base + " 놀거리");
                result.add(base + " 체험");
                result.add(base + " 테마파크");
                result.add(base + " 유원지");
                result.add(base + " 액티비티");
            }
            case PHOTO_SPOT -> {
                result.add(base + " 사진 명소");
                result.add(base + " 포토존");
                result.add(base + " 전망대");
                result.add(base + " 감성 명소");
                result.add(base + " 핫플");
            }
            case DRIVE -> {
                result.add(base + " 드라이브코스");
                result.add(base + " 드라이브 코스");
                result.add(base + " 해안도로");
                result.add(base + " 드라이브 루트");
                result.add(base + " 드라이브하기 좋은 곳");
                result.add(base + " 드라이브 명소");
                result.add(base + " 고개");
                result.add(base + " 해변 도로");
                result.add(base + " 가볼만한곳");
            }
            default -> {
                result.add(base + " 명소");
                result.add(base + " 관광지");
                result.add(base + " 대표 관광지");
                result.add(base + " 가볼만한곳");
                result.add(base + " 핫플");
                result.add(base + " 핫플레이스");
                result.add(base + " 놀거리");
                result.add(base + " 볼거리");
                result.add(base + " 데이트코스");
                result.add(base + " 전망대");
                result.add(base + " 공원");
            }
        }
    }

    private String expandProvinceName(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        String compact = value.replaceAll("\\s+", "");

        if ("경상북".equals(compact) || "경북".equals(compact)) {
            return "경상북도";
        }

        if ("경상남".equals(compact) || "경남".equals(compact)) {
            return "경상남도";
        }

        if ("전라북".equals(compact) || "전북".equals(compact)) {
            return "전라북도";
        }

        if ("전라남".equals(compact) || "전남".equals(compact)) {
            return "전라남도";
        }

        if ("충청북".equals(compact) || "충북".equals(compact)) {
            return "충청북도";
        }

        if ("충청남".equals(compact) || "충남".equals(compact)) {
            return "충청남도";
        }

        if ("제주".equals(compact) || "제주도".equals(compact)) {
            return "제주특별자치도";
        }

        if ("강원".equals(compact) || "강원도".equals(compact)) {
            return "강원특별자치도";
        }

        return value;
    }

    private void addNightViewSpecialQueries(Set<String> result, String base) {
        String normalizedBase = base.replaceAll("\\s+", "");

        if (normalizedBase.contains("광안리") || normalizedBase.contains("광안")) {
            result.add("부산 광안대교 야경");
            result.add("부산 민락수변공원 야경");
            result.add("부산 황령산 전망대");
            result.add("부산 금련산 전망대");
        }

        if (normalizedBase.contains("해운대")) {
            result.add("부산 더베이101 야경");
            result.add("부산 마린시티 야경");
            result.add("부산 동백섬 야경");
            result.add("부산 청사포 야경");
            result.add("부산 해월전망대");
        }

        if (normalizedBase.contains("잠실")) {
            result.add("서울 롯데월드타워 전망대");
            result.add("서울 석촌호수 야경");
            result.add("서울 잠실한강공원 야경");
        }

        if (normalizedBase.contains("인천국제공항") || normalizedBase.contains("인천공항")) {
            result.add("인천공항 전망대");
            result.add("인천 영종도 전망대");
            result.add("인천 월미도 야경");
            result.add("인천대교 전망대");
        }
    }
}