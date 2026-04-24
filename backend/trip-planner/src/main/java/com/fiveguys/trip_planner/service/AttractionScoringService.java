package com.fiveguys.trip_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AttractionScoringService {

    private final AttractionFilterService attractionFilterService;

    public AttractionScoringService(AttractionFilterService attractionFilterService) {
        this.attractionFilterService = attractionFilterService;
    }

    public int score(JsonNode doc,
                     String destination,
                     String detailArea,
                     String neighborhood,
                     String district) {
        return score(doc, destination, detailArea, neighborhood, district, AttractionSubIntent.GENERAL);
    }

    public int score(JsonNode doc,
                     String destination,
                     String detailArea,
                     String neighborhood,
                     String district,
                     AttractionSubIntent subIntent) {
        int score = 0;

        String name = AttractionTextHelper.clean(doc.path("place_name").asText());
        String mergedAddress = combinedAddress(doc);
        String category = AttractionTextHelper.clean(doc.path("category_name").asText());
        String categoryGroupCode = AttractionTextHelper.clean(doc.path("category_group_code").asText());

        if ("AT4".equals(categoryGroupCode)) {
            score += 20;
        }

        if (StringUtils.hasText(destination)) {
            if (attractionFilterService.containsLooseRegion(mergedAddress, destination)) {
                score += 12;
            }

            if (attractionFilterService.containsLooseRegion(name, destination)) {
                score += 4;
            }
        }

        if (StringUtils.hasText(detailArea)) {
            if (attractionFilterService.containsLooseRegion(mergedAddress, detailArea)) {
                score += 18;
            }

            if (attractionFilterService.containsLooseRegion(name, detailArea)) {
                score += 10;
            }
        }

        if (StringUtils.hasText(neighborhood)) {
            if (attractionFilterService.containsLooseRegion(mergedAddress, neighborhood)) {
                score += 15;
            }

            if (attractionFilterService.containsLooseRegion(name, neighborhood)) {
                score += 8;
            }
        }

        if (StringUtils.hasText(district) && !AttractionTextHelper.isCityOrCounty(district)) {
            if (attractionFilterService.containsLooseRegion(mergedAddress, district)) {
                score += 10;
            }
        }

        if (AttractionTextHelper.containsKeyword(category,
                "관광명소", "유적", "공원", "전망대", "해변",
                "유원지", "테마파크", "수목원", "휴양림", "해수욕장")) {
            score += 6;
        }

        if (AttractionTextHelper.containsKeyword(category,
                "오름", "해안도로", "드라이브코스", "해수욕장", "해변",
                "공원", "랜드마크", "테마파크", "수목원", "휴양림",
                "온천", "저수지", "동물원", "식물원", "수족관",
                "유적지", "문화재", "성", "궁", "사찰", "절",
                "폭포", "계곡", "호수", "섬", "항구", "포구",
                "시장", "거리", "테마거리", "벽화마을", "전통시장",
                "제주올레길", "둘레길", "산책로", "등산로")) {
            score += 10;
        }

        if (AttractionTextHelper.containsKeyword(category,
                "오름", "해안도로", "드라이브코스", "해수욕장", "해변")) {
            score += 8;
        }

        if (AttractionTextHelper.containsKeyword(name,
                "전망대", "공원", "해안도로", "해변", "해수욕장", "오름",
                "봉", "산", "숲", "수목원", "휴양림", "테마파크",
                "랜드마크", "거리", "시장", "마을", "성", "궁",
                "사찰", "절", "폭포", "계곡", "호수", "섬", "포구",
                "항구", "올레길", "둘레길", "산책로")) {
            score += 5;
        }

        if (AttractionTextHelper.containsKeyword(category,
                "박물관", "미술관", "전시관", "문화시설")) {
            if (subIntent == AttractionSubIntent.INDOOR) {
                score += 15;
            } else {
                score -= 8;
            }
        }

        score += scoreBySubIntent(name, category, subIntent);

        if (AttractionTextHelper.containsKeyword(name,
                "공항", "국제공항", "터미널", "버스터미널", "여객터미널", "역")) {
            score -= 18;
        }

        if (AttractionTextHelper.containsKeyword(name,
                "공항전망대", "여객터미널", "터미널")) {
            score -= 15;
        }

        if (AttractionTextHelper.containsKeyword(name,
                "인증대", "중간인증대", "종점인증대", "안내소", "관리소",
                "관리사무소", "매표소", "주차장", "화장실", "출입구", "입구")) {
            score -= 15;
        }

        if (AttractionTextHelper.containsKeyword(name,
                "주민센터", "행정복지센터", "센터", "지구대", "파출소",
                "우체국", "은행", "마트", "편의점")) {
            score -= 10;
        }

        if (AttractionTextHelper.containsKeyword(category,
                "주차장", "공중화장실", "화장실", "관공서", "행정기관",
                "편의시설", "교통시설")) {
            score -= 10;
        }

        if (AttractionTextHelper.containsKeyword(category,
                "숙박", "음식점", "카페", "주점", "술집", "호프", "포차")) {
            score -= 20;
        }

        if (StringUtils.hasText(doc.path("road_address_name").asText())) {
            score += 2;
        }

        if (StringUtils.hasText(doc.path("phone").asText())) {
            score += 1;
        }

        return score;
    }

    private int scoreBySubIntent(String name, String category, AttractionSubIntent subIntent) {
        AttractionSubIntent resolvedSubIntent = subIntent == null ? AttractionSubIntent.GENERAL : subIntent;

        int score = 0;

        switch (resolvedSubIntent) {
            case HOT_PLACE -> {
                if (AttractionTextHelper.containsKeyword(name, "거리", "테마거리", "시장", "마을", "랜드마크", "핫플", "광장", "해변", "공원")) {
                    score += 18;
                }
                if (AttractionTextHelper.containsKeyword(category, "거리", "테마거리", "랜드마크", "시장", "전통시장", "공원", "해수욕장")) {
                    score += 12;
                }
            }
            case DATE_COURSE -> {
                if (AttractionTextHelper.containsKeyword(name, "공원", "산책로", "호수", "해변", "전망대", "거리", "야경", "수목원", "대교", "해수욕장")) {
                    score += 20;
                }
                if (AttractionTextHelper.containsKeyword(category, "공원", "산책로", "해변", "전망대", "수목원", "테마거리", "해수욕장")) {
                    score += 12;
                }
            }
            case WALK -> {
                if (AttractionTextHelper.containsKeyword(name, "산책로", "둘레길", "올레길", "공원", "숲", "해변", "호수", "강변", "수변공원", "해수욕장")) {
                    score += 22;
                }
                if (AttractionTextHelper.containsKeyword(category, "산책로", "둘레길", "공원", "해변", "수목원", "휴양림", "해수욕장")) {
                    score += 12;
                }
            }
            case NIGHT_VIEW -> {
                if (AttractionTextHelper.containsKeyword(name, "야경", "전망대", "타워", "랜드마크", "대교", "스카이", "루프", "전망", "수변공원")) {
                    score += 30;
                }
                if (AttractionTextHelper.containsKeyword(category, "전망대", "랜드마크", "대교")) {
                    score += 20;
                }
                if (AttractionTextHelper.containsKeyword(name, "박물관", "미술관", "전시관", "문화관", "기념관")) {
                    score -= 20;
                }
            }
            case NATURE -> {
                if (AttractionTextHelper.containsKeyword(name, "오름", "숲", "해변", "해수욕장", "바다", "수목원", "휴양림", "계곡", "폭포", "호수", "산", "공원")) {
                    score += 24;
                }
                if (AttractionTextHelper.containsKeyword(category, "오름", "해변", "해수욕장", "수목원", "휴양림", "폭포", "계곡", "산", "공원")) {
                    score += 14;
                }
                if (AttractionTextHelper.containsKeyword(name, "박물관", "미술관", "전시관")) {
                    score -= 6;
                }
            }
            case INDOOR -> {
                if (AttractionTextHelper.containsKeyword(name,
                        "아쿠아리움", "수족관", "체험관", "복합문화공간", "문화공간",
                        "공연장", "쇼핑몰", "몰", "센터", "전망대", "도서관")) {
                    score += 28;
                }

                if (AttractionTextHelper.containsKeyword(name,
                        "박물관", "미술관", "전시관", "기념관", "문화관")) {
                    score += 18;
                }

                if (AttractionTextHelper.containsKeyword(category,
                        "수족관", "공연장", "문화시설", "쇼핑센터", "복합문화공간")) {
                    score += 18;
                }

                if (AttractionTextHelper.containsKeyword(category,
                        "박물관", "미술관", "전시관")) {
                    score += 10;
                }

                if (AttractionTextHelper.containsKeyword(name,
                        "해변", "해수욕장", "오름", "산책로", "둘레길")) {
                    score -= 10;
                }
            }
            case ACTIVITY -> {
                if (AttractionTextHelper.containsKeyword(name, "테마파크", "유원지", "체험", "월드", "랜드", "동물원", "아쿠아리움", "수족관")) {
                    score += 24;
                }
                if (AttractionTextHelper.containsKeyword(category, "테마파크", "유원지", "동물원", "수족관", "체험")) {
                    score += 14;
                }
            }
            case PHOTO_SPOT -> {
                if (AttractionTextHelper.containsKeyword(name, "전망대", "해변", "거리", "마을", "벽화", "랜드마크", "정원", "수목원", "공원", "다리", "대교", "오름", "해수욕장")) {
                    score += 22;
                }
                if (AttractionTextHelper.containsKeyword(category, "전망대", "랜드마크", "테마거리", "공원", "수목원", "해수욕장")) {
                    score += 12;
                }
            }
            case DRIVE -> {
                if (AttractionTextHelper.containsKeyword(name, "해안도로", "드라이브", "전망대", "해변", "대교", "고개", "항구", "포구", "해수욕장")) {
                    score += 24;
                }
                if (AttractionTextHelper.containsKeyword(category, "해안도로", "드라이브코스", "전망대", "해변", "항구", "해수욕장")) {
                    score += 14;
                }
            }
            default -> {
                return 0;
            }
        }

        return score;
    }

    private String combinedAddress(JsonNode doc) {
        String roadAddress = AttractionTextHelper.clean(doc.path("road_address_name").asText());
        String addressName = AttractionTextHelper.clean(doc.path("address_name").asText());

        if (StringUtils.hasText(roadAddress) && StringUtils.hasText(addressName)) {
            return roadAddress + " | " + addressName;
        }

        if (StringUtils.hasText(roadAddress)) {
            return roadAddress;
        }

        return addressName;
    }
}