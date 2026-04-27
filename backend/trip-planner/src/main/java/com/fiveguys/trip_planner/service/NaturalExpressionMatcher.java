package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NaturalExpressionMatcher {

    public boolean isRestaurantExpression(String value) {
        String normalized = normalize(value);

        return containsAny(normalized,
                "밥 먹", "밥먹", "밥 먹을", "밥먹을",
                "먹을 데", "먹을데", "먹을 곳", "먹을곳",
                "먹을만한 데", "먹을만한데", "먹을 만한 데", "먹을 만한데",
                "뭐 먹", "뭐먹", "뭘 먹", "뭘먹",
                "뭐 먹지", "뭐먹지", "뭐 먹을까", "뭐먹을까",
                "먹고 싶", "먹고싶", "먹으러",
                "먹고 잘", "먹고잘", "먹고 자", "먹고자", "먹고 묵", "먹고묵",
                "먹고 놀", "먹고놀", "놀고 먹", "놀고먹",
                "한끼", "한 끼", "끼니",
                "식사", "식사할", "식사할 만한", "식사할만한",
                "배고픈데", "배고파", "출출",
                "로컬 맛집", "로컬맛집", "현지인 맛집", "현지인맛집", "현지 맛집", "현지맛집",
                "가볍게 먹을", "간단히 먹을", "간단하게 먹을",
                "점심", "저녁", "아침", "브런치", "야식",
                "커피 마실", "커피마실", "커피 한잔", "커피한잔",
                "디저트 먹", "빵 먹",
                "술 한잔", "술한잔", "한잔할", "한 잔 할",
                "회식", "데이트 밥", "데이트밥", "데이트 맛집", "데이트맛집",
                "혼밥", "혼자 먹");
    }

    public boolean isStayExpression(String value) {
        String normalized = normalize(value);

        return containsAny(normalized,
                "잘 데", "잘데", "잘 곳", "잘곳",
                "잠잘 데", "잠잘데", "잠잘 곳", "잠잘곳",
                "묵을 데", "묵을데", "묵을 곳", "묵을곳",
                "머물 데", "머물데", "머물 곳", "머물곳",
                "자고", "자고 놀", "자고놀",
                "먹고 잘", "먹고잘", "먹고 자", "먹고자", "먹고 묵", "먹고묵",
                "하룻밤", "하루 묵", "하루 잘",
                "숙박할", "숙박하기",
                "체크인", "체크 인",
                "호텔 잡", "방 잡", "방잡",
                "잠만 잘", "잠만잘",
                "쉬고 갈", "쉬어갈", "쉬어 갈",
                "가성비 숙소", "가성비숙소", "저렴한 숙소", "저렴한숙소", "싼 숙소", "싼숙소",
                "감성 숙소", "감성숙소", "예쁜 숙소", "예쁜숙소",
                "오션뷰", "바다뷰", "뷰 좋은", "뷰좋은",
                "한옥 숙소", "한옥숙소", "한옥스테이",
                "풀빌라", "수영장 숙소", "수영장숙소",
                "가족 숙소", "가족숙소", "커플 숙소", "커플숙소",
                "혼자 묵", "혼자묵", "혼자 잘", "혼자잘",
                "공항 근처 숙소", "역 근처 숙소");
    }

    public boolean isAttractionExpression(String value) {
        String normalized = normalize(value);

        return containsAny(normalized,
                "어디 갈", "어디갈", "어디 가", "어디가",
                "어디 가면", "어디가면",
                "어디 가야", "어디가야",
                "갈 데", "갈데", "갈 곳", "갈곳",
                "놀 데", "놀데", "놀 곳", "놀곳",
                "놀만한 곳", "놀만한곳", "놀 만한 곳", "놀 만한곳",
                "구경할 데", "구경할데", "구경할 곳", "구경할곳",
                "볼만한 데", "볼만한데", "볼 만한 데", "볼 만한데",
                "볼만한 곳", "볼 만한 곳",
                "가볼 데", "가볼데", "가볼 곳", "가볼곳",
                "가야 할 곳", "가야할 곳", "가야할곳",
                "들를 곳", "들를곳", "들릴 곳", "들릴곳",
                "시간 보내기", "시간보내기",
                "시간 보내기 좋은", "시간보내기 좋은",
                "시간 보낼", "시간보낼",
                "시간 보낼 곳", "시간보낼곳",
                "시간 때울", "시간때울",
                "먹고 놀", "먹고놀", "자고 놀", "자고놀", "놀고 먹", "놀고먹",
                "가볍게 들를", "잠깐 들를",
                "근처 볼만한", "근처 볼 만한",
                "사진 찍을", "사진찍을",
                "인생샷", "포토존",
                "산책할", "걷기 좋은", "걸을만한",
                "데이트할", "데이트 하기",
                "밤에 갈", "밤에갈",
                "야경 볼", "야경볼",
                "비올 때 갈", "비 올 때 갈",
                "실내 갈", "실내에서",
                "아이랑 갈", "아이와 갈",
                "가족이랑 갈", "커플끼리 갈",
                "드라이브할", "차로 갈",
                "바다 보러", "숲 보러",
                "힐링할", "쉬러 갈");
    }

    public boolean isItineraryExpression(String value) {
        String normalized = normalize(value);

        return containsAny(normalized,
                "일정 짜", "일정짜",
                "코스 짜", "코스짜",
                "계획 짜", "계획짜",
                "여행 계획", "여행계획",
                "여행 루트", "여행루트",
                "동선", "루트",
                "하루 코스", "당일 코스",
                "여행 코스", "여행코스",
                "돌아다닐 순서",
                "어떻게 돌", "어떻게돌",
                "일정 추천", "코스 추천",
                "여행 일정", "여행일정");
    }

    public boolean isCombinedExpression(String value) {
        String normalized = normalize(value);

        return containsAny(normalized,
                "이랑", "랑", "하고", "과", "와", "및",
                "함께", "같이", "둘 다", "둘다",
                "도 같이", "도같이", "까지 같이", "까지같이",
                "먹고 잘", "먹고잘",
                "먹고 자", "먹고자",
                "먹고 묵", "먹고묵",
                "먹고 놀", "먹고놀",
                "자고 놀", "자고놀",
                "놀고 먹", "놀고먹",
                "숙소 맛집", "맛집 숙소",
                "숙소 관광지", "관광지 숙소",
                "맛집 명소", "명소 맛집",
                "맛집 관광지", "관광지 맛집",
                "일정 맛집", "맛집 일정",
                "일정 숙소", "숙소 일정",
                "일정 관광지", "관광지 일정");
    }

    public boolean isRestaurantStrongExpression(String value) {
        String normalized = normalize(value);

        return containsAny(normalized,
                "밥 먹을", "먹을 데", "먹을데", "먹을 곳", "먹을곳",
                "뭐 먹", "뭐먹", "뭘 먹", "뭘먹",
                "뭐 먹지", "뭐먹지", "뭐 먹을까", "뭐먹을까",
                "먹고 잘", "먹고잘", "먹고 자", "먹고자", "먹고 묵", "먹고묵",
                "먹고 놀", "먹고놀", "놀고 먹", "놀고먹",
                "식사할", "한끼", "한 끼",
                "배고픈데", "로컬 맛집", "현지인 맛집",
                "커피 마실", "술 한잔", "한잔할");
    }

    public boolean isStayStrongExpression(String value) {
        String normalized = normalize(value);

        return containsAny(normalized,
                "잘 데", "잘데", "잘 곳", "잘곳",
                "잠잘 데", "잠잘데", "잠잘 곳", "잠잘곳",
                "묵을 데", "묵을데", "묵을 곳", "묵을곳",
                "머물 데", "머물데", "머물 곳", "머물곳",
                "먹고 잘", "먹고잘", "먹고 자", "먹고자", "먹고 묵", "먹고묵",
                "자고 놀", "자고놀",
                "호텔 잡", "방 잡", "체크인");
    }

    public boolean isAttractionStrongExpression(String value) {
        String normalized = normalize(value);

        return containsAny(normalized,
                "어디 갈", "어디갈", "갈 데", "갈데",
                "놀 데", "놀데", "놀 곳", "놀곳",
                "구경할 데", "구경할데",
                "볼만한 데", "볼만한데",
                "가볼 데", "가볼데", "가볼 곳", "가볼곳",
                "시간 보내기", "시간보내기",
                "시간 보낼", "시간보낼",
                "시간 때울", "시간때울",
                "먹고 놀", "먹고놀", "자고 놀", "자고놀", "놀고 먹", "놀고먹",
                "사진 찍을", "인생샷", "포토존",
                "산책할", "야경 볼", "드라이브할");
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }
}