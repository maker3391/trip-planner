package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;

@Service
public class AttractionSubIntentResolver {

    public AttractionSubIntent resolve(String message) {
        String value = AttractionTextHelper.normalizeForMatch(message == null ? "" : message);

        if (AttractionTextHelper.containsKeyword(value,
                "실내", "비올때", "비 오는 날", "전시", "박물관", "미술관", "전시관", "아쿠아리움",
                "수족관", "체험관", "복합문화공간", "공연장", "쇼핑몰")) {
            return AttractionSubIntent.INDOOR;
        }

        if (AttractionTextHelper.containsKeyword(value, "야경", "밤에", "밤", "나이트뷰")) {
            return AttractionSubIntent.NIGHT_VIEW;
        }

        if (AttractionTextHelper.containsKeyword(value, "데이트", "데이트코스", "커플")) {
            return AttractionSubIntent.DATE_COURSE;
        }

        if (AttractionTextHelper.containsKeyword(value, "산책", "걷기", "걷기좋은", "산책로", "둘레길", "올레길")) {
            return AttractionSubIntent.WALK;
        }

        if (AttractionTextHelper.containsKeyword(value, "자연", "숲", "오름", "해변", "바다", "수목원", "휴양림", "계곡", "폭포")) {
            return AttractionSubIntent.NATURE;
        }

        if (AttractionTextHelper.containsKeyword(value, "놀거리", "체험", "액티비티", "테마파크", "유원지")) {
            return AttractionSubIntent.ACTIVITY;
        }

        if (AttractionTextHelper.containsKeyword(value, "사진", "포토존", "사진찍기", "사진 찍기", "감성", "인생샷")) {
            return AttractionSubIntent.PHOTO_SPOT;
        }

        if (AttractionTextHelper.containsKeyword(value, "드라이브", "차로", "해안도로", "드라이브코스")) {
            return AttractionSubIntent.DRIVE;
        }

        if (AttractionTextHelper.containsKeyword(value, "핫플", "핫플레이스", "요즘", "인기", "트렌디")) {
            return AttractionSubIntent.HOT_PLACE;
        }

        return AttractionSubIntent.GENERAL;
    }
}