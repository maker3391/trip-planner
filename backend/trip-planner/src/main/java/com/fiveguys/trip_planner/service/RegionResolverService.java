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
            if (StringUtils.hasText(resolvedDistrict)) {
                String districtHead = extractSigHead(resolvedDistrict);
                if (isCityOrCounty(districtHead)) {
                    resolvedCity = districtHead;
                } else if (isCityOrCounty(resolvedDistrict)) {
                    resolvedCity = resolvedDistrict;
                }
            }

            if (!StringUtils.hasText(resolvedCity) && neighborhood != null && StringUtils.hasText(neighborhood.getCity())) {
                resolvedCity = neighborhood.getCity();
            }

            if (!StringUtils.hasText(resolvedCity) && StringUtils.hasText(provinceName)) {
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

            if ("CTPRVN".equals(region.getLevel())) {
                if (matchesTopLevel(tokens, normalizedMessage, region.getName())) {
                    return true;
                }
                continue;
            }

            if ("SIG".equals(region.getLevel())) {
                if (matchesTopLevel(tokens, normalizedMessage, region.getName())) {
                    return true;
                }

                String sigHead = extractSigHead(region.getName());
                if (StringUtils.hasText(sigHead) && matchesTopLevel(tokens, normalizedMessage, sigHead)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchesTopLevel(List<String> tokens, String normalizedMessage, String regionName) {
        String normalizedName = normalize(regionName);
        String strippedName = stripSuffix(normalizedName);

        if (tokens.contains(normalizedName)) {
            return true;
        }

        if (StringUtils.hasText(strippedName) && tokens.contains(strippedName)) {
            return true;
        }

        return isExactPhraseHit(normalizedMessage, normalizedName);
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
        String districtHead = extractSigHead(districtName);

        if (isCityOrCounty(districtHead)) {
            return districtHead;
        }

        if (isCityOrCounty(districtName)) {
            return districtName;
        }

        if (StringUtils.hasText(district.getCity())) {
            return district.getCity();
        }

        return provinceName;
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

            if (StringUtils.hasText(parentName) && !isSameAreaName(region.getParent(), parentName)) {
                continue;
            }

            if (StringUtils.hasText(provinceName) && !isSameAreaName(region.getCity(), provinceName)) {
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

            if (StringUtils.hasText(cityFilter) && !isSameAreaName(region.getCity(), cityFilter)) {
                continue;
            }

            if (StringUtils.hasText(parentFilter) && !isSameAreaName(region.getParent(), parentFilter)) {
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

        if ("CTPRVN".equals(level)) {
            return tokens.contains(normalizedName)
                    || (StringUtils.hasText(strippedName) && tokens.contains(strippedName))
                    || isExactPhraseHit(normalizedMessage, normalizedName);
        }

        if ("SIG".equals(level)) {
            if (tokens.contains(normalizedName)
                    || (StringUtils.hasText(strippedName) && tokens.contains(strippedName))
                    || isExactPhraseHit(normalizedMessage, normalizedName)) {
                return true;
            }

            String sigHead = extractSigHead(region.getName());
            if (StringUtils.hasText(sigHead)) {
                String normalizedHead = normalize(sigHead);
                String strippedHead = stripSuffix(normalizedHead);

                return tokens.contains(normalizedHead)
                        || (StringUtils.hasText(strippedHead) && tokens.contains(strippedHead))
                        || isExactPhraseHit(normalizedMessage, normalizedHead);
            }
        }

        if ("EMD".equals(level)) {
            if (tokens.contains(normalizedName) || isExactPhraseHit(normalizedMessage, normalizedName)) {
                return true;
            }

            String strippedOnly = stripSuffix(normalizedName);
            return StringUtils.hasText(strippedOnly)
                    && strippedOnly.length() >= 2
                    && tokens.contains(strippedOnly)
                    && !looksLikeBroadAmbiguousArea(strippedOnly);
        }

        return false;
    }

    private int matchScore(String normalizedMessage, List<String> tokens, RegionRecord region) {
        int score = 0;

        String normalizedName = normalize(region.getName());
        String strippedName = stripSuffix(normalizedName);

        if (tokens.contains(normalizedName)) {
            score += 100;
        }

        if ("SIG".equals(region.getLevel()) || "CTPRVN".equals(region.getLevel())) {
            if (StringUtils.hasText(strippedName) && tokens.contains(strippedName)) {
                score += 70;
            }
        }

        if ("EMD".equals(region.getLevel())) {
            if (StringUtils.hasText(strippedName)
                    && strippedName.length() >= 2
                    && tokens.contains(strippedName)
                    && !looksLikeBroadAmbiguousArea(strippedName)) {
                score += 35;
            }
        }

        if (isExactPhraseHit(normalizedMessage, normalizedName)) {
            score += 20;
        }

        if ("SIG".equals(region.getLevel())) {
            String sigHead = extractSigHead(region.getName());
            if (StringUtils.hasText(sigHead)) {
                String normalizedHead = normalize(sigHead);
                String strippedHead = stripSuffix(normalizedHead);

                if (tokens.contains(normalizedHead)) {
                    score += 80;
                }

                if (StringUtils.hasText(strippedHead) && tokens.contains(strippedHead)) {
                    score += 55;
                }

                if (isExactPhraseHit(normalizedMessage, normalizedHead)) {
                    score += 18;
                }
            }
        }

        if (StringUtils.hasText(region.getParent()) && tokens.contains(normalize(region.getParent()))) {
            score += 12;
        }

        if (StringUtils.hasText(region.getCity())) {
            String normalizedCity = normalize(region.getCity());
            String strippedCity = stripSuffix(normalizedCity);

            if (tokens.contains(normalizedCity)) {
                score += 10;
            } else if (StringUtils.hasText(strippedCity) && tokens.contains(strippedCity)) {
                score += 8;
            }
        }

        return score;
    }

    private boolean isSameAreaName(String left, String right) {
        if (!StringUtils.hasText(left) || !StringUtils.hasText(right)) {
            return false;
        }

        String normalizedLeft = normalize(left);
        String normalizedRight = normalize(right);

        if (normalizedLeft.equals(normalizedRight)) {
            return true;
        }

        String strippedLeft = stripSuffix(normalizedLeft);
        String strippedRight = stripSuffix(normalizedRight);

        return StringUtils.hasText(strippedLeft)
                && StringUtils.hasText(strippedRight)
                && strippedLeft.equals(strippedRight);
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

    private String extractSigHead(String sigName) {
        if (!StringUtils.hasText(sigName)) {
            return "";
        }

        String[] parts = sigName.trim().split("\\s+");
        if (parts.length == 0) {
            return "";
        }

        String first = parts[0];
        if (first.endsWith("시") || first.endsWith("군") || first.endsWith("구")) {
            return first;
        }

        return "";
    }

    private boolean looksLikeBroadAmbiguousArea(String value) {
        if (!StringUtils.hasText(value)) {
            return true;
        }

        return value.length() <= 1
                || "중".equals(value)
                || "서".equals(value)
                || "동".equals(value)
                || "남".equals(value)
                || "북".equals(value);
    }

    private boolean isCityOrCounty(String value) {
        return StringUtils.hasText(value)
                && (value.endsWith("시") || value.endsWith("군"));
    }

    private String stripSuffix(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.replaceAll("(특별자치도|특별자치시|광역시|특별시|시|군|구|동|읍|면|리)$", "").trim();
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