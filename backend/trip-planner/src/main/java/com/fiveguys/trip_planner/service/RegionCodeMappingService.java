package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.RegionCodeInfo;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RegionCodeMappingService {

    private static final Map<String, String> AREA_CODE_MAP = new LinkedHashMap<>();
    private static final Map<String, RegionCodeInfo> DETAIL_REGION_CODE_MAP = new LinkedHashMap<>();

    static {
        AREA_CODE_MAP.put("서울", "11");
        AREA_CODE_MAP.put("부산", "26");
        AREA_CODE_MAP.put("대구", "27");
        AREA_CODE_MAP.put("인천", "28");
        AREA_CODE_MAP.put("광주", "29");
        AREA_CODE_MAP.put("대전", "30");
        AREA_CODE_MAP.put("울산", "31");
        AREA_CODE_MAP.put("세종", "36");
        AREA_CODE_MAP.put("경기", "41");
        AREA_CODE_MAP.put("충북", "43");
        AREA_CODE_MAP.put("충남", "44");
        AREA_CODE_MAP.put("전남", "46");
        AREA_CODE_MAP.put("경북", "47");
        AREA_CODE_MAP.put("경남", "48");
        AREA_CODE_MAP.put("제주", "50");
        AREA_CODE_MAP.put("강원", "51");
        AREA_CODE_MAP.put("전북", "52");

        AREA_CODE_MAP.put("여수", "46");
        AREA_CODE_MAP.put("순천", "46");
        AREA_CODE_MAP.put("목포", "46");
        AREA_CODE_MAP.put("경주", "47");
        AREA_CODE_MAP.put("포항", "47");
        AREA_CODE_MAP.put("강릉", "51");
        AREA_CODE_MAP.put("속초", "51");
        AREA_CODE_MAP.put("춘천", "51");
        AREA_CODE_MAP.put("전주", "52");
        AREA_CODE_MAP.put("군산", "52");

        DETAIL_REGION_CODE_MAP.put("서면", new RegionCodeInfo("부산", "26", "26230"));
        DETAIL_REGION_CODE_MAP.put("전포", new RegionCodeInfo("부산", "26", "26230"));
        DETAIL_REGION_CODE_MAP.put("남포동", new RegionCodeInfo("부산", "26", "26110"));
        DETAIL_REGION_CODE_MAP.put("해운대", new RegionCodeInfo("부산", "26", "26350"));
        DETAIL_REGION_CODE_MAP.put("광안리", new RegionCodeInfo("부산", "26", "26500"));

        DETAIL_REGION_CODE_MAP.put("홍대", new RegionCodeInfo("서울", "11", "11440"));
        DETAIL_REGION_CODE_MAP.put("연남동", new RegionCodeInfo("서울", "11", "11440"));
        DETAIL_REGION_CODE_MAP.put("합정", new RegionCodeInfo("서울", "11", "11440"));
        DETAIL_REGION_CODE_MAP.put("상수", new RegionCodeInfo("서울", "11", "11440"));
        DETAIL_REGION_CODE_MAP.put("강남", new RegionCodeInfo("서울", "11", "11680"));
        DETAIL_REGION_CODE_MAP.put("명동", new RegionCodeInfo("서울", "11", "11140"));
        DETAIL_REGION_CODE_MAP.put("종로", new RegionCodeInfo("서울", "11", "11110"));
        DETAIL_REGION_CODE_MAP.put("익선동", new RegionCodeInfo("서울", "11", "11110"));
        DETAIL_REGION_CODE_MAP.put("성수", new RegionCodeInfo("서울", "11", "11200"));
        DETAIL_REGION_CODE_MAP.put("잠실", new RegionCodeInfo("서울", "11", "11710"));

        DETAIL_REGION_CODE_MAP.put("애월", new RegionCodeInfo("제주", "50", "50110"));
    }

    public RegionCodeInfo resolve(String destination, String detailArea) {
        if (detailArea != null && DETAIL_REGION_CODE_MAP.containsKey(detailArea)) {
            return DETAIL_REGION_CODE_MAP.get(detailArea);
        }

        if (destination == null) {
            return null;
        }

        String areaCode = AREA_CODE_MAP.get(destination);
        if (areaCode == null) {
            return null;
        }

        return new RegionCodeInfo(destination, areaCode, null);
    }
}