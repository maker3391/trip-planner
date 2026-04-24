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
            addAttractionQueries(result, base);
        }

        return new ArrayList<>(result);
    }

    public List<String> buildRelaxedFallbackQueries(String destination, String district) {
        Set<String> result = new LinkedHashSet<>();

        if (StringUtils.hasText(district) && !AttractionTextHelper.isCityOrCounty(district)) {
            addAttractionQueries(result, AttractionTextHelper.joinDistinctLocation(destination, district));
        }

        addAttractionQueries(result, destination);

        return new ArrayList<>(result);
    }

    private void addAttractionQueries(Set<String> result, String base) {
        if (!StringUtils.hasText(base)) {
            return;
        }

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
}