package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.RegionRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class RegionResolverService {

    private final RegionCsvLoader regionCsvLoader;

    public RegionResolverService(RegionCsvLoader regionCsvLoader) {
        this.regionCsvLoader = regionCsvLoader;
    }

    public ResolvedRegion resolve(String message) {
        String normalizedMessage = normalize(message);
        List<String> tokens = tokenize(message);

        RegionRecord province = findBestProvince(normalizedMessage, tokens);
        String provinceName = province != null ? province.getName() : "";

        RegionRecord district = findBestDistrict(normalizedMessage, tokens, provinceName);
        String resolvedDistrict = district != null ? district.getName() : "";

        String resolvedCity = resolveCity(provinceName, district);

        RegionRecord neighborhood = findBestNeighborhood(normalizedMessage, tokens, provinceName, resolvedDistrict);
        String resolvedNeighborhood = neighborhood != null ? neighborhood.getName() : "";

        if (!StringUtils.hasText(resolvedDistrict) && neighborhood != null && StringUtils.hasText(neighborhood.getParent())) {
            resolvedDistrict = neighborhood.getParent();
        }

        if (!StringUtils.hasText(resolvedCity)) {
            if (StringUtils.hasText(resolvedDistrict) && isCityOrCounty(resolvedDistrict)) {
                resolvedCity = resolvedDistrict;
            } else if (neighborhood != null && StringUtils.hasText(neighborhood.getCity())) {
                resolvedCity = neighborhood.getCity();
            } else if (StringUtils.hasText(provinceName)) {
                resolvedCity = provinceName;
            }
        }

        if (StringUtils.hasText(resolvedNeighborhood) && StringUtils.hasText(resolvedDistrict)) {
            RegionRecord verified = findNeighborhoodByNameAndParentAndProvince(
                    resolvedNeighborhood,
                    resolvedDistrict,
                    provinceName
            );
            if (verified == null) {
                resolvedNeighborhood = "";
            }
        }

        if (StringUtils.hasText(resolvedDistrict) && isDistrictOnly(resolvedDistrict) && StringUtils.hasText(provinceName)) {
            RegionRecord verifiedDistrict = findDistrictByNameAndProvince(resolvedDistrict, provinceName);
            if (verifiedDistrict == null) {
                resolvedDistrict = "";
                resolvedNeighborhood = "";
            }
        }

        String detailName = StringUtils.hasText(resolvedNeighborhood) ? resolvedNeighborhood : resolvedDistrict;

        return new ResolvedRegion(
                resolvedCity,
                resolvedDistrict,
                resolvedNeighborhood,
                detailName
        );
    }

    public boolean hasExplicitTopLevelArea(String message) {
        String normalizedMessage = normalize(message);
        List<String> tokens = tokenize(message);

        for (RegionRecord region : regionCsvLoader.getRegionRecords()) {
            if (!"CTPRVN".equals(region.getLevel()) && !"SIG".equals(region.getLevel())) {
                continue;
            }

            String name = region.getName();
            String normalizedName = normalize(name);
            String strippedName = stripSuffix(normalizedName);

            if ("CTPRVN".equals(region.getLevel())) {
                if (tokens.contains(normalizedName)
                        || (StringUtils.hasText(strippedName) && tokens.contains(strippedName))
                        || isExactPhraseHit(normalizedMessage, normalizedName)) {
                    return true;
                }
            }

            if ("SIG".equals(region.getLevel()) && isCityOrCounty(name)) {
                if (tokens.contains(normalizedName)
                        || (StringUtils.hasText(strippedName) && tokens.contains(strippedName))
                        || isExactPhraseHit(normalizedMessage, normalizedName)) {
                    return true;
                }
            }
        }

        return false;
    }

    private RegionRecord findBestProvince(String normalizedMessage, List<String> tokens) {
        return findBestMatch(normalizedMessage, tokens, "CTPRVN", null, null);
    }

    private RegionRecord findBestDistrict(String normalizedMessage, List<String> tokens, String provinceName) {
        RegionRecord byProvince = null;

        if (StringUtils.hasText(provinceName)) {
            byProvince = findBestMatch(normalizedMessage, tokens, "SIG", provinceName, null);
        }

        if (byProvince != null) {
            return byProvince;
        }

        return findBestMatch(normalizedMessage, tokens, "SIG", null, null);
    }

    private RegionRecord findBestNeighborhood(String normalizedMessage,
                                              List<String> tokens,
                                              String provinceName,
                                              String districtName) {
        if (StringUtils.hasText(districtName)) {
            RegionRecord exact = findBestMatch(normalizedMessage, tokens, "EMD", provinceName, districtName);
            if (exact != null) {
                return exact;
            }
        }

        if (StringUtils.hasText(provinceName)) {
            RegionRecord byProvince = findBestMatch(normalizedMessage, tokens, "EMD", provinceName, null);
            if (byProvince != null) {
                return byProvince;
            }
        }

        return null;
    }

    private String resolveCity(String provinceName, RegionRecord district) {
        if (district == null) {
            return provinceName;
        }

        String districtName = district.getName();

        if (isCityOrCounty(districtName)) {
            return districtName;
        }

        if (StringUtils.hasText(district.getCity())) {
            return district.getCity();
        }

        return provinceName;
    }

    private RegionRecord findDistrictByNameAndProvince(String districtName, String provinceName) {
        for (RegionRecord region : regionCsvLoader.getRegionRecords()) {
            if (!"SIG".equals(region.getLevel())) {
                continue;
            }

            if (!normalize(region.getName()).equals(normalize(districtName))) {
                continue;
            }

            if (StringUtils.hasText(provinceName) && !normalize(region.getCity()).equals(normalize(provinceName))) {
                continue;
            }

            return region;
        }

        return null;
    }

    private RegionRecord findNeighborhoodByNameAndParentAndProvince(String neighborhoodName,
                                                                    String parentName,
                                                                    String provinceName) {
        for (RegionRecord region : regionCsvLoader.getRegionRecords()) {
            if (!"EMD".equals(region.getLevel())) {
                continue;
            }

            if (!normalize(region.getName()).equals(normalize(neighborhoodName))) {
                continue;
            }

            if (StringUtils.hasText(parentName) && !normalize(region.getParent()).equals(normalize(parentName))) {
                continue;
            }

            if (StringUtils.hasText(provinceName) && !normalize(region.getCity()).equals(normalize(provinceName))) {
                continue;
            }

            return region;
        }

        return null;
    }

    private RegionRecord findBestMatch(String normalizedMessage,
                                       List<String> tokens,
                                       String level,
                                       String cityFilter,
                                       String parentFilter) {
        List<RegionRecord> candidates = new ArrayList<>();

        for (RegionRecord region : regionCsvLoader.getRegionRecords()) {
            if (!level.equals(region.getLevel())) {
                continue;
            }

            if (StringUtils.hasText(cityFilter) && !normalize(region.getCity()).equals(normalize(cityFilter))) {
                continue;
            }

            if (StringUtils.hasText(parentFilter) && !normalize(region.getParent()).equals(normalize(parentFilter))) {
                continue;
            }

            if (matchesRegion(normalizedMessage, tokens, region)) {
                candidates.add(region);
            }
        }

        return candidates.stream()
                .max(Comparator
                        .comparingInt((RegionRecord region) -> matchScore(normalizedMessage, tokens, region))
                        .thenComparingInt(region -> normalize(region.getName()).length()))
                .orElse(null);
    }

    private boolean matchesRegion(String normalizedMessage, List<String> tokens, RegionRecord region) {
        String level = region.getLevel();
        String normalizedName = normalize(region.getName());
        String strippedName = stripSuffix(normalizedName);

        if ("CTPRVN".equals(level) || "SIG".equals(level)) {
            if (tokens.contains(normalizedName)) {
                return true;
            }

            if (StringUtils.hasText(strippedName) && tokens.contains(strippedName)) {
                return true;
            }

            return isExactPhraseHit(normalizedMessage, normalizedName);
        }

        if ("EMD".equals(level)) {
            if (tokens.contains(normalizedName)) {
                return true;
            }

            return isExactPhraseHit(normalizedMessage, normalizedName);
        }

        return false;
    }

    private int matchScore(String normalizedMessage, List<String> tokens, RegionRecord region) {
        String normalizedName = normalize(region.getName());
        String strippedName = stripSuffix(normalizedName);
        int score = 0;

        if (tokens.contains(normalizedName)) {
            score += 100;
        }

        if (!"EMD".equals(region.getLevel()) && StringUtils.hasText(strippedName) && tokens.contains(strippedName)) {
            score += 70;
        }

        if (isExactPhraseHit(normalizedMessage, normalizedName)) {
            score += 20;
        }

        if (StringUtils.hasText(region.getParent()) && tokens.contains(normalize(region.getParent()))) {
            score += 12;
        }

        if (StringUtils.hasText(region.getCity()) && tokens.contains(normalize(region.getCity()))) {
            score += 10;
        }

        return score;
    }

    private boolean isExactPhraseHit(String normalizedMessage, String normalizedName) {
        if (!StringUtils.hasText(normalizedMessage) || !StringUtils.hasText(normalizedName)) {
            return false;
        }

        String paddedMessage = " " + normalizedMessage + " ";
        String paddedName = " " + normalizedName + " ";

        return paddedMessage.contains(paddedName);
    }

    private List<String> tokenize(String message) {
        String normalized = normalize(message);
        if (!StringUtils.hasText(normalized)) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (String token : normalized.split("\\s+")) {
            if (StringUtils.hasText(token)) {
                result.add(token);
            }
        }
        return result;
    }

    private boolean isCityOrCounty(String value) {
        return StringUtils.hasText(value)
                && (value.endsWith("시") || value.endsWith("군"));
    }

    private boolean isDistrictOnly(String value) {
        return StringUtils.hasText(value) && value.endsWith("구");
    }

    private String stripSuffix(String value) {
        return value.replaceAll("(특별자치도|특별자치시|광역시|특별시|시|군|구|동|읍|면|리)$", "");
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static class ResolvedRegion {
        private final String city;
        private final String district;
        private final String neighborhood;
        private final String detailName;

        public ResolvedRegion(String city, String district, String neighborhood, String detailName) {
            this.city = city;
            this.district = district;
            this.neighborhood = neighborhood;
            this.detailName = detailName;
        }

        public String getCity() {
            return city;
        }

        public String getDistrict() {
            return district;
        }

        public String getNeighborhood() {
            return neighborhood;
        }

        public String getDetailName() {
            return detailName;
        }
    }
}