package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.RegionTarget;
import com.fiveguys.trip_planner.dto.ResolvedRegion;
import com.fiveguys.trip_planner.exception.LlmCallException;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AreaCodeService {

    private static final String CSV_PATH = "regions.csv";

    private final Map<String, String> areaCodeByAreaName = new HashMap<>();
    private final Map<String, List<RegionTarget>> targetsByAreaName = new HashMap<>();
    private final Map<String, RegionTarget> sigunguByName = new HashMap<>();
    private final Map<String, String> areaAliasToCanonical = new HashMap<>();

    @PostConstruct
    public void init() {
        loadCsv();
        buildAreaAliases();
    }

    public ResolvedRegion resolve(String destination) {
        if (!StringUtils.hasText(destination)) {
            throw new LlmCallException("여행 목적지는 필수입니다.");
        }

        String normalized = normalize(destination);

        RegionTarget sigunguTarget = sigunguByName.get(normalized);
        if (sigunguTarget != null) {
            return new ResolvedRegion(
                    destination,
                    normalized,
                    List.of(sigunguTarget),
                    true,
                    sigunguTarget.getSigunguName()
            );
        }

        if ("전라도".equals(normalized)) {
            return resolveMultiArea(destination, normalized, List.of("전남", "전북"));
        }

        if ("경상도".equals(normalized)) {
            return resolveMultiArea(destination, normalized, List.of("경남", "경북"));
        }

        if ("충청도".equals(normalized)) {
            return resolveMultiArea(destination, normalized, List.of("충남", "충북"));
        }

        String canonicalArea = areaAliasToCanonical.get(normalized);
        if (canonicalArea != null) {
            List<RegionTarget> targets = targetsByAreaName.getOrDefault(canonicalArea, List.of());
            return new ResolvedRegion(
                    destination,
                    normalized,
                    targets,
                    false,
                    null
            );
        }

        throw new LlmCallException("현재는 시/도 또는 구/군 단위 지역명만 지원합니다. 예: 부산, 강원도, 해운대구, 강릉시");
    }

    public List<RegionTarget> getTargetsByAreaName(String areaName) {
        return new ArrayList<>(targetsByAreaName.getOrDefault(areaName, List.of()));
    }

    public RegionTarget getSigunguTarget(String sigunguName) {
        if (!StringUtils.hasText(sigunguName)) {
            return null;
        }
        return sigunguByName.get(normalize(sigunguName));
    }

    private ResolvedRegion resolveMultiArea(String originalInput,
                                            String normalizedInput,
                                            List<String> canonicalAreas) {
        List<RegionTarget> mergedTargets = new ArrayList<>();

        for (String canonicalArea : canonicalAreas) {
            mergedTargets.addAll(targetsByAreaName.getOrDefault(canonicalArea, List.of()));
        }

        return new ResolvedRegion(
                originalInput,
                normalizedInput,
                mergedTargets,
                false,
                null
        );
    }

    private void loadCsv() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new ClassPathResource(CSV_PATH).getInputStream(), StandardCharsets.UTF_8))) {

            String line = br.readLine();
            if (line == null) {
                throw new IllegalStateException("regions.csv is empty.");
            }

            while ((line = br.readLine()) != null) {
                if (!StringUtils.hasText(line)) {
                    continue;
                }

                String[] tokens = line.split(",", -1);
                if (tokens.length < 4) {
                    continue;
                }

                String areaCd = tokens[0].trim();
                String areaNm = tokens[1].trim();
                String sigunguCd = tokens[2].trim();
                String sigunguNm = tokens[3].trim();

                String canonicalAreaName = toCanonicalAreaName(areaNm);

                areaCodeByAreaName.putIfAbsent(canonicalAreaName, areaCd);

                RegionTarget target = new RegionTarget(
                        canonicalAreaName,
                        areaCd,
                        sigunguNm,
                        sigunguCd
                );

                targetsByAreaName
                        .computeIfAbsent(canonicalAreaName, k -> new ArrayList<>())
                        .add(target);

                registerSigungu(sigunguNm, target);
            }

        } catch (Exception e) {
            throw new IllegalStateException("regions.csv loading failed.", e);
        }
    }

    private void registerSigungu(String sigunguNm, RegionTarget target) {
        sigunguByName.put(normalize(sigunguNm), target);

        String aliasWithoutSuffix = removeSigunguSuffix(sigunguNm);
        if (StringUtils.hasText(aliasWithoutSuffix) && aliasWithoutSuffix.length() >= 2) {
            sigunguByName.putIfAbsent(normalize(aliasWithoutSuffix), target);
        }
    }

    private void buildAreaAliases() {
        registerAreaAlias("서울", "서울");
        registerAreaAlias("서울시", "서울");
        registerAreaAlias("서울특별시", "서울");

        registerAreaAlias("부산", "부산");
        registerAreaAlias("부산시", "부산");
        registerAreaAlias("부산광역시", "부산");

        registerAreaAlias("대구", "대구");
        registerAreaAlias("대구시", "대구");
        registerAreaAlias("대구광역시", "대구");

        registerAreaAlias("인천", "인천");
        registerAreaAlias("인천시", "인천");
        registerAreaAlias("인천광역시", "인천");

        registerAreaAlias("광주", "광주");
        registerAreaAlias("광주시", "광주");
        registerAreaAlias("광주광역시", "광주");

        registerAreaAlias("대전", "대전");
        registerAreaAlias("대전시", "대전");
        registerAreaAlias("대전광역시", "대전");

        registerAreaAlias("울산", "울산");
        registerAreaAlias("울산시", "울산");
        registerAreaAlias("울산광역시", "울산");

        registerAreaAlias("세종", "세종");
        registerAreaAlias("세종시", "세종");
        registerAreaAlias("세종특별자치시", "세종");

        registerAreaAlias("경기", "경기");
        registerAreaAlias("경기도", "경기");

        registerAreaAlias("강원", "강원");
        registerAreaAlias("강원도", "강원");
        registerAreaAlias("강원특별자치도", "강원");

        registerAreaAlias("충북", "충북");
        registerAreaAlias("충청북도", "충북");

        registerAreaAlias("충남", "충남");
        registerAreaAlias("충청남도", "충남");

        registerAreaAlias("전북", "전북");
        registerAreaAlias("전라북도", "전북");
        registerAreaAlias("전북특별자치도", "전북");

        registerAreaAlias("전남", "전남");
        registerAreaAlias("전라남도", "전남");

        registerAreaAlias("경북", "경북");
        registerAreaAlias("경상북도", "경북");

        registerAreaAlias("경남", "경남");
        registerAreaAlias("경상남도", "경남");

        registerAreaAlias("제주", "제주");
        registerAreaAlias("제주도", "제주");
        registerAreaAlias("제주특별자치도", "제주");
    }

    private void registerAreaAlias(String alias, String canonical) {
        areaAliasToCanonical.put(normalize(alias), canonical);
    }

    private String removeSigunguSuffix(String sigunguNm) {
        String value = sigunguNm.trim();
        if (value.endsWith("시") || value.endsWith("군") || value.endsWith("구")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String toCanonicalAreaName(String areaNm) {
        String normalized = normalize(areaNm);

        if (normalized.contains("서울")) return "서울";
        if (normalized.contains("부산")) return "부산";
        if (normalized.contains("대구")) return "대구";
        if (normalized.contains("인천")) return "인천";
        if (normalized.contains("광주")) return "광주";
        if (normalized.contains("대전")) return "대전";
        if (normalized.contains("울산")) return "울산";
        if (normalized.contains("세종")) return "세종";
        if (normalized.contains("경기")) return "경기";
        if (normalized.contains("강원")) return "강원";
        if (normalized.contains("충청북")) return "충북";
        if (normalized.contains("충청남")) return "충남";
        if (normalized.contains("전라북") || normalized.contains("전북")) return "전북";
        if (normalized.contains("전라남")) return "전남";
        if (normalized.contains("경상북")) return "경북";
        if (normalized.contains("경상남")) return "경남";
        if (normalized.contains("제주")) return "제주";

        throw new IllegalStateException("Unsupported area name: " + areaNm);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replaceAll("\\s+", "")
                .replace("특별시", "")
                .replace("광역시", "")
                .replace("특별자치시", "")
                .replace("특별자치도", "")
                .replace("자치시", "")
                .replace("자치도", "")
                .toLowerCase();
    }
}