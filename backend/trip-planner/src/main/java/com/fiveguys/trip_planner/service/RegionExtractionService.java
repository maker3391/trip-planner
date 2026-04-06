package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionExtractionService {

    private static final List<String> REGIONS = List.of(
            "서울", "부산", "대구", "인천", "광주", "대전", "울산",
            "세종", "경기", "강원", "충북", "충남", "전북",
            "전남", "경북", "경남", "제주",
            "여수", "순천", "목포", "경주", "포항", "강릉", "속초", "춘천", "전주", "군산"
    );

    public String extract(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        for (String region : REGIONS) {
            if (text.contains(region)) {
                return region;
            }
        }

        return null;
    }
}