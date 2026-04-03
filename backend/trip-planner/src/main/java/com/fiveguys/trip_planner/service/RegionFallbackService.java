package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.RegionTarget;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RegionFallbackService {

    private static final String CSV_PATH = "region_fallback.csv";

    private final Map<String, List<String>> nearbySigunguMap = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        loadCsv();
    }

    public List<RegionTarget> buildFallbackTargets(RegionTarget primaryTarget,
                                                   List<RegionTarget> sameAreaTargets) {
        List<RegionTarget> result = new ArrayList<>();

        if (primaryTarget == null || sameAreaTargets == null || sameAreaTargets.isEmpty()) {
            return result;
        }

        Map<String, RegionTarget> bySigunguName = new LinkedHashMap<>();
        for (RegionTarget target : sameAreaTargets) {
            bySigunguName.put(normalize(target.getSigunguName()), target);
        }

        List<String> nearbyNames = nearbySigunguMap.getOrDefault(
                normalize(primaryTarget.getSigunguName()),
                List.of()
        );

        for (String nearbyName : nearbyNames) {
            RegionTarget nearby = bySigunguName.get(normalize(nearbyName));
            if (nearby != null && !nearby.getSigunguCode().equals(primaryTarget.getSigunguCode())) {
                result.add(nearby);
            }
        }

        return result;
    }

    private void loadCsv() {
        try {
            ClassPathResource resource = new ClassPathResource(CSV_PATH);
            if (!resource.exists()) {
                return;
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String line = br.readLine(); // header
                if (line == null) {
                    return;
                }

                while ((line = br.readLine()) != null) {
                    if (!StringUtils.hasText(line)) {
                        continue;
                    }

                    String[] tokens = line.split(",", -1);
                    if (tokens.length < 2) {
                        continue;
                    }

                    String sigunguName = normalize(tokens[0]);
                    List<String> nearbyList = new ArrayList<>();

                    for (int i = 1; i < tokens.length; i++) {
                        String value = normalize(tokens[i]);
                        if (StringUtils.hasText(value)) {
                            nearbyList.add(value);
                        }
                    }

                    if (StringUtils.hasText(sigunguName) && !nearbyList.isEmpty()) {
                        nearbySigunguMap.put(sigunguName, nearbyList);
                    }
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException("region_fallback.csv loading failed.", e);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replaceAll("\\s+", "")
                .toLowerCase();
    }
}