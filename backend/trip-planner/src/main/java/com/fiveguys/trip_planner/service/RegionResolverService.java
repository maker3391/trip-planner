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

        RegionRecord province = findBestMatch(normalizedMessage, "CTPRVN", null, null);
        RegionRecord district = findBestMatch(normalizedMessage, "SIG", null, null);

        String resolvedCity = province != null ? province.getName() : "";
        String resolvedDistrict = district != null ? district.getName() : "";

        if (!StringUtils.hasText(resolvedCity) && district != null) {
            if (isMetroStyleDistrict(district)) {
                resolvedCity = district.getCity();
            } else {
                resolvedCity = district.getName();
            }
        }

        RegionRecord narrowedDistrict = null;
        if (StringUtils.hasText(resolvedCity)) {
            narrowedDistrict = findBestMatch(
                    normalizedMessage,
                    "SIG",
                    isProvinceLike(resolvedCity) ? resolvedCity : "",
                    null
            );
        }

        if (narrowedDistrict != null) {
            resolvedDistrict = narrowedDistrict.getName();

            if (!StringUtils.hasText(resolvedCity)) {
                resolvedCity = isMetroStyleDistrict(narrowedDistrict)
                        ? narrowedDistrict.getCity()
                        : narrowedDistrict.getName();
            }
        }

        RegionRecord neighborhood = findBestNeighborhood(normalizedMessage, resolvedCity, resolvedDistrict);

        if (neighborhood == null && StringUtils.hasText(resolvedCity)) {
            neighborhood = findBestMatch(
                    normalizedMessage,
                    "EMD",
                    isProvinceLike(resolvedCity) ? resolvedCity : "",
                    null
            );
        }

        String resolvedNeighborhood = neighborhood != null ? neighborhood.getName() : "";

        if (!StringUtils.hasText(resolvedDistrict) && neighborhood != null && StringUtils.hasText(neighborhood.getParent())) {
            resolvedDistrict = neighborhood.getParent();
        }

        if (!StringUtils.hasText(resolvedCity) && neighborhood != null) {
            if (StringUtils.hasText(neighborhood.getCity())) {
                resolvedCity = neighborhood.getCity();
            }
        }

        if (StringUtils.hasText(resolvedCity) && StringUtils.hasText(resolvedDistrict)) {
            if (isProvinceLike(resolvedCity)) {
                String districtCity = districtCityOf(resolvedDistrict);
                if (StringUtils.hasText(districtCity) && !normalize(districtCity).equals(normalize(resolvedCity))) {
                    resolvedDistrict = "";
                    resolvedNeighborhood = "";
                }
            }
        }

        if (StringUtils.hasText(resolvedNeighborhood) && StringUtils.hasText(resolvedDistrict)) {
            RegionRecord verifiedNeighborhood = findNeighborhoodByNameCityParent(
                    resolvedNeighborhood,
                    resolvedCity,
                    resolvedDistrict
            );
            if (verifiedNeighborhood == null) {
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

    private RegionRecord findBestNeighborhood(String normalizedMessage, String resolvedCity, String resolvedDistrict) {
        String cityFilter = isProvinceLike(resolvedCity) ? resolvedCity : "";
        String parentFilter = StringUtils.hasText(resolvedDistrict) ? resolvedDistrict : "";

        RegionRecord exactParentMatch = findBestMatch(normalizedMessage, "EMD", cityFilter, parentFilter);
        if (exactParentMatch != null) {
            return exactParentMatch;
        }

        if (StringUtils.hasText(parentFilter)) {
            return findBestMatch(normalizedMessage, "EMD", "", parentFilter);
        }

        return findBestMatch(normalizedMessage, "EMD", cityFilter, null);
    }

    private RegionRecord findNeighborhoodByNameCityParent(String neighborhoodName, String city, String parent) {
        for (RegionRecord region : regionCsvLoader.getRegionRecords()) {
            if (!"EMD".equals(region.getLevel())) {
                continue;
            }

            if (!normalize(region.getName()).equals(normalize(neighborhoodName))) {
                continue;
            }

            if (StringUtils.hasText(city) && StringUtils.hasText(region.getCity())) {
                if (!normalize(region.getCity()).equals(normalize(city))) {
                    continue;
                }
            }

            if (StringUtils.hasText(parent) && StringUtils.hasText(region.getParent())) {
                if (!normalize(region.getParent()).equals(normalize(parent))) {
                    continue;
                }
            }

            return region;
        }

        return null;
    }

    private RegionRecord findBestMatch(String normalizedMessage,
                                       String level,
                                       String cityFilter,
                                       String parentFilter) {
        List<RegionRecord> candidates = new ArrayList<>();

        for (RegionRecord region : regionCsvLoader.getRegionRecords()) {
            if (!level.equals(region.getLevel())) {
                continue;
            }

            if (StringUtils.hasText(cityFilter) && StringUtils.hasText(region.getCity())) {
                if (!normalize(region.getCity()).equals(normalize(cityFilter))) {
                    continue;
                }
            }

            if (StringUtils.hasText(parentFilter) && StringUtils.hasText(region.getParent())) {
                if (!normalize(region.getParent()).equals(normalize(parentFilter))) {
                    continue;
                }
            }

            String normalizedName = normalize(region.getName());
            String strippedName = stripSuffix(normalizedName);

            if (normalizedMessage.contains(normalizedName) || normalizedMessage.contains(strippedName)) {
                candidates.add(region);
            }
        }

        return candidates.stream()
                .max(Comparator
                        .comparingInt((RegionRecord region) -> matchScore(normalizedMessage, region))
                        .thenComparingInt(region -> stripSuffix(normalize(region.getName())).length())
                        .thenComparingInt(region -> normalize(region.getName()).length()))
                .orElse(null);
    }

    private int matchScore(String normalizedMessage, RegionRecord region) {
        String normalizedName = normalize(region.getName());
        String strippedName = stripSuffix(normalizedName);

        int score = 0;

        if (normalizedMessage.contains(normalizedName)) {
            score += 20;
        }

        if (normalizedMessage.contains(strippedName)) {
            score += 12;
        }

        if (StringUtils.hasText(region.getParent()) && normalizedMessage.contains(normalize(region.getParent()))) {
            score += 8;
        }

        if (StringUtils.hasText(region.getCity()) && normalizedMessage.contains(normalize(region.getCity()))) {
            score += 6;
        }

        return score;
    }

    private boolean isMetroStyleDistrict(RegionRecord district) {
        return district != null
                && StringUtils.hasText(district.getCity())
                && normalize(district.getCity()).equals(normalize(district.getParent()));
    }

    private boolean isProvinceLike(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String normalized = normalize(value);

        for (RegionRecord region : regionCsvLoader.getRegionRecords()) {
            if (!"CTPRVN".equals(region.getLevel())) {
                continue;
            }

            if (normalize(region.getName()).equals(normalized)) {
                return true;
            }
        }

        return false;
    }

    private String districtCityOf(String districtName) {
        for (RegionRecord region : regionCsvLoader.getRegionRecords()) {
            if (!"SIG".equals(region.getLevel())) {
                continue;
            }

            if (normalize(region.getName()).equals(normalize(districtName))) {
                return region.getCity();
            }
        }
        return "";
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
                .replaceAll("\\s+", "")
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