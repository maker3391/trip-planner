package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class RestaurantKeywordService {

    public boolean containsRestaurantFoodKeyword(String message) {
        return !extractRestaurantFoodKeywords(message).isEmpty();
    }

    public boolean isCafeFocusedRequest(String message) {
        String value = normalize(message);
        return containsAny(value,
                "카페", "디저트", "베이커리", "빵집", "케이크", "빙수", "와플", "도넛", "아이스크림",
                "cafe", "dessert", "bakery"
        );
    }

    public boolean isPubFocusedRequest(String message) {
        String value = normalize(message);
        return containsAny(value,
                "술집", "주점", "포차", "호프", "와인바", "칵테일바",
                "pub", "bar", "wine", "cocktail"
        );
    }

    public List<String> extractRestaurantFoodKeywords(String message) {
        String value = normalize(message);
        LinkedHashSet<String> result = new LinkedHashSet<>();

        if (!StringUtils.hasText(value)) {
            return new ArrayList<>(result);
        }

        mapNaturalExpressions(value, result);

        addIfContains(value, result, "돼지국밥", "돼지국밥");
        addIfContains(value, result, "국밥", "국밥");
        addIfContains(value, result, "순대국", "순대국");
        addIfContains(value, result, "순댓국", "순대국");
        addIfContains(value, result, "설렁탕", "설렁탕");
        addIfContains(value, result, "곰탕", "곰탕");
        addIfContains(value, result, "갈비탕", "갈비탕");
        addIfContains(value, result, "해장국", "해장국");
        addIfContains(value, result, "감자탕", "감자탕");
        addIfContains(value, result, "김치찌개", "김치찌개");
        addIfContains(value, result, "된장찌개", "된장찌개");
        addIfContains(value, result, "청국장", "청국장");
        addIfContains(value, result, "순두부", "순두부");
        addIfContains(value, result, "순두부찌개", "순두부");
        addIfContains(value, result, "부대찌개", "부대찌개");
        addIfContains(value, result, "동태찌개", "동태찌개");
        addIfContains(value, result, "매운탕", "매운탕");
        addIfContains(value, result, "추어탕", "추어탕");
        addIfContains(value, result, "삼계탕", "삼계탕");
        addIfContains(value, result, "백숙", "백숙");
        addIfContains(value, result, "닭한마리", "닭한마리");
        addIfContains(value, result, "전골", "전골");
        addIfContains(value, result, "곱도리탕", "곱도리탕");
        addIfContains(value, result, "닭볶음탕", "닭볶음탕");
        addIfContains(value, result, "아구찜", "아구찜");
        addIfContains(value, result, "아귀찜", "아구찜");
        addIfContains(value, result, "해물탕", "해물탕");
        addIfContains(value, result, "해물찜", "해물찜");
        addIfContains(value, result, "샤브샤브", "샤브샤브");

        addIfContains(value, result, "백반", "백반");
        addIfContains(value, result, "한정식", "한정식");
        addIfContains(value, result, "비빔밥", "비빔밥");
        addIfContains(value, result, "돌솥비빔밥", "비빔밥");
        addIfContains(value, result, "쌈밥", "쌈밥");
        addIfContains(value, result, "보리밥", "보리밥");
        addIfContains(value, result, "기사식당", "기사식당");
        addIfContains(value, result, "꼬막비빔밥", "꼬막비빔밥");

        addIfContains(value, result, "불고기", "불고기");
        addIfContains(value, result, "갈비", "갈비");
        addIfContains(value, result, "돼지갈비", "돼지갈비");
        addIfContains(value, result, "소갈비", "소갈비");
        addIfContains(value, result, "양념갈비", "양념갈비");
        addIfContains(value, result, "생갈비", "생갈비");
        addIfContains(value, result, "삼겹살", "삼겹살");
        addIfContains(value, result, "목살", "목살");
        addIfContains(value, result, "항정살", "항정살");
        addIfContains(value, result, "가브리살", "가브리살");
        addIfContains(value, result, "족발", "족발");
        addIfContains(value, result, "보쌈", "보쌈");
        addIfContains(value, result, "닭갈비", "닭갈비");
        addIfContains(value, result, "제육볶음", "제육볶음");
        addIfContains(value, result, "오리구이", "오리구이");
        addIfContains(value, result, "장어구이", "장어구이");
        addIfContains(value, result, "생선구이", "생선구이");
        addIfContains(value, result, "게장", "게장");
        addIfContains(value, result, "간장게장", "게장");
        addIfContains(value, result, "양념게장", "게장");
        addIfContains(value, result, "육회", "육회");
        addIfContains(value, result, "육사시미", "육사시미");
        addIfContains(value, result, "닭발", "닭발");
        addIfContains(value, result, "쭈꾸미", "쭈꾸미");
        addIfContains(value, result, "주꾸미", "쭈꾸미");
        addIfContains(value, result, "오징어볶음", "오징어볶음");
        addIfContains(value, result, "낙곱새", "낙곱새");

        addIfContains(value, result, "한우", "한우");
        addIfContains(value, result, "소고기", "소고기");
        addIfContains(value, result, "꽃등심", "꽃등심");
        addIfContains(value, result, "등심", "등심");
        addIfContains(value, result, "안심", "안심");
        addIfContains(value, result, "차돌박이", "차돌박이");
        addIfContains(value, result, "토시살", "토시살");
        addIfContains(value, result, "살치살", "살치살");
        addIfContains(value, result, "안창살", "안창살");
        addIfContains(value, result, "갈비살", "갈비살");
        addIfContains(value, result, "업진살", "업진살");
        addIfContains(value, result, "제비추리", "제비추리");
        addIfContains(value, result, "부채살", "부채살");

        addIfContains(value, result, "곱창", "곱창");
        addIfContains(value, result, "대창", "대창");
        addIfContains(value, result, "막창", "막창");
        addIfContains(value, result, "소막창", "소막창");
        addIfContains(value, result, "돼지막창", "돼지막창");
        addIfContains(value, result, "흑돼지", "흑돼지");

        if (value.contains("고깃집") || value.contains("고기집") || value.contains("고기 맛있는 곳")) {
            result.add("고기집");
        }
        if (value.contains("막창집")) {
            result.add("막창");
        }
        if (value.contains("곱창집")) {
            result.add("곱창");
        }
        if (value.contains("갈비집")) {
            result.add("갈비");
        }
        if (value.contains("국밥집")) {
            result.add("국밥");
        }
        if (value.contains("찌개집")) {
            result.add("김치찌개");
        }
        if (value.contains("소고기집")) {
            result.add("소고기");
        }

        addIfContains(value, result, "냉면", "냉면");
        addIfContains(value, result, "밀면", "밀면");
        addIfContains(value, result, "칼국수", "칼국수");
        addIfContains(value, result, "수제비", "수제비");
        addIfContains(value, result, "국수", "국수");
        addIfContains(value, result, "막국수", "막국수");
        addIfContains(value, result, "잔치국수", "국수");
        addIfContains(value, result, "비빔국수", "국수");

        if (value.contains("스시")) result.add("초밥");
        addIfContains(value, result, "초밥", "초밥");
        addIfContains(value, result, "회", "횟집");
        addIfContains(value, result, "횟집", "횟집");
        addIfContains(value, result, "사시미", "사시미");
        addIfContains(value, result, "라멘", "라멘");
        addIfContains(value, result, "우동", "우동");
        addIfContains(value, result, "소바", "소바");
        addIfContains(value, result, "돈까스", "돈까스");
        addIfContains(value, result, "돈카츠", "돈까스");
        addIfContains(value, result, "텐동", "텐동");
        addIfContains(value, result, "덮밥", "덮밥");
        addIfContains(value, result, "오마카세", "오마카세");
        addIfContains(value, result, "이자카야", "이자카야");
        addIfContains(value, result, "일식", "일식");

        addIfContains(value, result, "짜장면", "짜장면");
        addIfContains(value, result, "짬뽕", "짬뽕");
        addIfContains(value, result, "탕수육", "탕수육");
        addIfContains(value, result, "마라탕", "마라탕");
        addIfContains(value, result, "마라샹궈", "마라샹궈");
        addIfContains(value, result, "훠궈", "훠궈");
        addIfContains(value, result, "양꼬치", "양꼬치");
        addIfContains(value, result, "딤섬", "딤섬");
        addIfContains(value, result, "중식", "중식");

        addIfContains(value, result, "파스타", "파스타");
        addIfContains(value, result, "스테이크", "스테이크");
        addIfContains(value, result, "리조또", "리조또");
        addIfContains(value, result, "피자", "피자");
        addIfContains(value, result, "브런치", "브런치");
        addIfContains(value, result, "샐러드", "샐러드");
        addIfContains(value, result, "수제버거", "수제버거");
        if (value.contains("햄버거") || value.contains("버거")) {
            result.add("버거");
        }
        addIfContains(value, result, "양식", "양식");
        addIfContains(value, result, "바베큐", "바베큐");

        addIfContains(value, result, "떡볶이", "떡볶이");
        addIfContains(value, result, "김밥", "김밥");
        addIfContains(value, result, "순대", "순대");
        addIfContains(value, result, "튀김", "튀김");
        addIfContains(value, result, "분식", "분식");
        addIfContains(value, result, "라볶이", "라볶이");
        addIfContains(value, result, "오뎅", "오뎅");
        addIfContains(value, result, "토스트", "토스트");

        addIfContains(value, result, "치킨", "치킨");
        addIfContains(value, result, "닭강정", "닭강정");

        addIfContains(value, result, "카페", "카페");
        addIfContains(value, result, "디저트", "디저트");
        addIfContains(value, result, "베이커리", "베이커리");
        addIfContains(value, result, "빵집", "빵집");
        addIfContains(value, result, "케이크", "케이크");
        addIfContains(value, result, "빙수", "빙수");
        addIfContains(value, result, "와플", "와플");
        addIfContains(value, result, "도넛", "도넛");
        addIfContains(value, result, "아이스크림", "아이스크림");

        addIfContains(value, result, "술집", "주점");
        addIfContains(value, result, "주점", "주점");
        addIfContains(value, result, "포차", "포차");
        addIfContains(value, result, "호프", "호프");
        addIfContains(value, result, "와인바", "와인바");
        addIfContains(value, result, "칵테일바", "칵테일바");

        return new ArrayList<>(result);
    }

    public List<String> expandRestaurantKeywordVariants(String keyword) {
        LinkedHashSet<String> result = new LinkedHashSet<>();

        switch (keyword) {
            case "돼지국밥":
                addVariants(result, "돼지국밥", "국밥", "국밥 맛집", "국밥 식당", "한식");
                break;
            case "국밥":
            case "순대국":
            case "설렁탕":
            case "곰탕":
            case "갈비탕":
            case "해장국":
            case "감자탕":
            case "추어탕":
            case "삼계탕":
            case "백숙":
                addVariants(result, keyword, keyword + " 맛집", keyword + " 식당", "국물요리", "한식");
                break;
            case "김치찌개":
            case "된장찌개":
            case "청국장":
            case "순두부":
            case "부대찌개":
            case "동태찌개":
            case "매운탕":
            case "전골":
            case "곱도리탕":
            case "닭볶음탕":
            case "아구찜":
            case "해물탕":
            case "해물찜":
            case "샤브샤브":
            case "닭한마리":
                addVariants(result, keyword, keyword + " 맛집", keyword + " 식당", "찌개", "탕", "한식");
                break;
            case "백반":
            case "한정식":
            case "비빔밥":
            case "쌈밥":
            case "보리밥":
            case "기사식당":
            case "꼬막비빔밥":
                addVariants(result, keyword, keyword + " 맛집", keyword + " 식당", "한식");
                break;
            case "불고기":
            case "삼겹살":
            case "목살":
            case "항정살":
            case "가브리살":
            case "족발":
            case "보쌈":
            case "닭갈비":
            case "제육볶음":
            case "오리구이":
            case "장어구이":
            case "생선구이":
            case "게장":
            case "육회":
            case "육사시미":
            case "닭발":
            case "쭈꾸미":
            case "오징어볶음":
            case "낙곱새":
            case "고기집":
            case "흑돼지":
                addVariants(result, keyword, keyword + " 맛집", keyword + " 식당", "고기집", "구이", "한식");
                break;
            case "한우":
            case "소고기":
            case "꽃등심":
            case "등심":
            case "안심":
            case "차돌박이":
            case "토시살":
            case "살치살":
            case "안창살":
            case "갈비살":
            case "업진살":
            case "제비추리":
            case "부채살":
                addVariants(result, keyword, keyword + " 맛집", keyword + " 식당", "소고기", "한우", "고기집", "구이", "한식");
                break;
            case "갈비":
            case "돼지갈비":
            case "소갈비":
            case "양념갈비":
            case "생갈비":
                addVariants(result, keyword, keyword + " 맛집", keyword + " 식당", "갈비", "고기집", "구이", "한식");
                break;
            case "곱창":
            case "대창":
            case "막창":
            case "소막창":
            case "돼지막창":
                addVariants(result, keyword, keyword + " 맛집", keyword + "집", "막창집", "곱창집", "고기집", "구이", "한식");
                break;
            case "냉면":
            case "밀면":
            case "칼국수":
            case "수제비":
            case "국수":
            case "막국수":
                addVariants(result, keyword, keyword + " 맛집", keyword + " 식당", "면요리", "한식");
                break;
            case "초밥":
            case "사시미":
            case "오마카세":
                addVariants(result, keyword, keyword + " 맛집", "스시", "일식");
                break;
            case "횟집":
                addVariants(result, "횟집", "회", "회 맛집", "횟집 추천", "일식");
                break;
            case "라멘":
            case "우동":
            case "소바":
            case "돈까스":
            case "텐동":
            case "덮밥":
            case "이자카야":
                addVariants(result, keyword, keyword + " 맛집", keyword + "집", "일식");
                break;
            case "일식":
                addVariants(result, "일식", "일식 맛집");
                break;
            case "짜장면":
            case "짬뽕":
            case "탕수육":
            case "마라탕":
            case "마라샹궈":
            case "훠궈":
            case "양꼬치":
            case "딤섬":
                addVariants(result, keyword, keyword + " 맛집", keyword + "집", "중식");
                break;
            case "중식":
                addVariants(result, "중식", "중식 맛집");
                break;
            case "파스타":
            case "스테이크":
            case "리조또":
            case "피자":
            case "브런치":
            case "샐러드":
            case "버거":
            case "수제버거":
            case "바베큐":
                addVariants(result, keyword, keyword + " 맛집", keyword + " 식당", "양식");
                break;
            case "양식":
                addVariants(result, "양식", "양식 맛집");
                break;
            case "떡볶이":
            case "김밥":
            case "순대":
            case "튀김":
            case "라볶이":
            case "오뎅":
            case "토스트":
                addVariants(result, keyword, keyword + " 맛집", keyword + "집", "분식");
                break;
            case "분식":
                addVariants(result, "분식", "분식 맛집");
                break;
            case "치킨":
            case "닭강정":
                addVariants(result, keyword, keyword + " 맛집", keyword + "집");
                break;
            case "카페":
            case "디저트":
            case "베이커리":
            case "빵집":
            case "케이크":
            case "빙수":
            case "와플":
            case "도넛":
            case "아이스크림":
                addVariants(result, keyword, keyword + " 추천", "카페");
                break;
            case "주점":
            case "포차":
            case "호프":
            case "와인바":
            case "칵테일바":
                addVariants(result, keyword, "술집", "주점");
                break;
            default:
                addVariants(result, keyword, keyword + " 맛집");
                break;
        }

        return new ArrayList<>(result);
    }

    public String resolveCacheSubtype(String message) {
        List<String> keywords = extractRestaurantFoodKeywords(message);

        if (keywords.isEmpty()) {
            return "restaurant";
        }

        String keyword = keywords.get(0);

        switch (keyword) {
            case "돼지국밥":
                return "dwaejigukbap";
            case "국밥":
            case "순대국":
            case "설렁탕":
            case "곰탕":
            case "갈비탕":
            case "해장국":
            case "감자탕":
            case "추어탕":
            case "삼계탕":
            case "백숙":
                return "soup";
            case "김치찌개":
            case "된장찌개":
            case "청국장":
            case "순두부":
            case "부대찌개":
            case "동태찌개":
            case "매운탕":
            case "전골":
            case "곱도리탕":
            case "닭볶음탕":
            case "아구찜":
            case "해물탕":
            case "해물찜":
            case "샤브샤브":
            case "닭한마리":
                return "jjigae";
            case "한우":
                return "hanwoo";
            case "소고기":
            case "꽃등심":
            case "등심":
            case "안심":
            case "차돌박이":
            case "토시살":
            case "살치살":
            case "안창살":
            case "갈비살":
            case "업진살":
            case "제비추리":
            case "부채살":
                return "beef";
            case "갈비":
            case "돼지갈비":
            case "소갈비":
            case "양념갈비":
            case "생갈비":
                return "galbi";
            case "삼겹살":
            case "목살":
            case "항정살":
            case "가브리살":
            case "불고기":
            case "족발":
            case "보쌈":
            case "닭갈비":
            case "제육볶음":
            case "오리구이":
            case "장어구이":
            case "생선구이":
            case "게장":
            case "육회":
            case "육사시미":
            case "닭발":
            case "쭈꾸미":
            case "오징어볶음":
            case "낙곱새":
            case "고기집":
            case "흑돼지":
                return "meat";
            case "곱창":
            case "대창":
            case "막창":
            case "소막창":
            case "돼지막창":
                return "gopchang";
            case "냉면":
            case "밀면":
            case "칼국수":
            case "수제비":
            case "국수":
            case "막국수":
                return "noodle";
            case "초밥":
            case "사시미":
            case "오마카세":
                return "sushi";
            case "횟집":
                return "hoetjip";
            case "라멘":
                return "ramen";
            case "우동":
                return "udon";
            case "소바":
                return "soba";
            case "돈까스":
                return "donkatsu";
            case "텐동":
                return "tendon";
            case "덮밥":
                return "donburi";
            case "이자카야":
                return "izakaya";
            case "일식":
                return "japanese";
            case "짜장면":
            case "짬뽕":
            case "탕수육":
            case "마라탕":
            case "마라샹궈":
            case "훠궈":
            case "양꼬치":
            case "딤섬":
            case "중식":
                return "chinese";
            case "파스타":
                return "pasta";
            case "스테이크":
                return "steak";
            case "리조또":
                return "risotto";
            case "피자":
                return "pizza";
            case "브런치":
                return "brunch";
            case "샐러드":
                return "salad";
            case "버거":
            case "수제버거":
                return "burger";
            case "바베큐":
                return "barbecue";
            case "양식":
                return "western";
            case "떡볶이":
            case "김밥":
            case "순대":
            case "튀김":
            case "분식":
            case "라볶이":
            case "오뎅":
            case "토스트":
                return "bunsik";
            case "치킨":
            case "닭강정":
                return "chicken";
            case "카페":
            case "디저트":
            case "베이커리":
            case "빵집":
            case "케이크":
            case "빙수":
            case "와플":
            case "도넛":
            case "아이스크림":
                return "cafe";
            case "주점":
            case "포차":
            case "호프":
            case "와인바":
            case "칵테일바":
                return "pub";
            default:
                return "restaurant";
        }
    }

    private void mapNaturalExpressions(String value, LinkedHashSet<String> result) {
        if (value.contains("해장할") || value.contains("해장할만한")) {
            result.add("해장국");
        }
        if (value.contains("국물") || value.contains("국물 있는")) {
            result.add("국밥");
        }
        if (value.contains("고기 땡")) {
            result.add("고기집");
        }
        if (value.contains("고기 먹고 싶")) {
            result.add("고기집");
        }
        if (value.contains("소고기 먹고 싶")) {
            result.add("소고기");
        }
        if (value.contains("술 한잔")) {
            result.add("주점");
        }
        if (value.contains("회 한접시")) {
            result.add("횟집");
        }
    }

    private void addIfContains(String value, LinkedHashSet<String> result, String trigger, String canonical) {
        if (value.contains(trigger)) {
            result.add(canonical);
        }
    }

    private void addVariants(LinkedHashSet<String> result, String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                result.add(value);
            }
        }
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.toLowerCase().trim();
    }
}