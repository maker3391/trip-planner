package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.RegionRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RegionResolverService {

    private final RegionCsvLoader regionCsvLoader;

    public RegionResolverService(RegionCsvLoader regionCsvLoader) {
        this.regionCsvLoader = regionCsvLoader;
    }

    public ResolvedRegion resolve(String message) {
        String normalizedMessage = normalize(message);
        List<String> tokens = tokenize(message);

        String broadProvinceAlias = resolveBroadProvinceAlias(tokens);

        RegionRecord explicitProvince = StringUtils.hasText(broadProvinceAlias)
                ? null
                : findExplicitProvince(tokens, normalizedMessage);

        RegionRecord province = StringUtils.hasText(broadProvinceAlias)
                ? null
                : (explicitProvince != null ? explicitProvince : findBestProvince(normalizedMessage, tokens));

        String provinceName = StringUtils.hasText(broadProvinceAlias)
                ? broadProvinceAlias
                : (province != null ? province.getName() : "");

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
                provinceName,
                resolvedCity,
                resolvedDistrict,
                resolvedNeighborhood,
                detailName
        );
    }

    public boolean hasExplicitTopLevelArea(String message) {
        String normalizedMessage = normalize(message);
        List<String> tokens = tokenize(message);

        if (tokens.contains("전라도") || tokens.contains("경상도") || tokens.contains("충청도")) {
            return true;
        }

        for (RegionRecord region : regionCsvLoader.getRegionRecords()) {
            if (!"CTPRVN".equals(region.getLevel()) && !"SIG".equals(region.getLevel())) {
                continue;
            }

            if (matchesTopLevel(tokens, normalizedMessage, region)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasExplicitProvinceArea(String message) {
        String normalizedMessage = normalize(message);
        List<String> tokens = tokenize(message);

        if (tokens.contains("전라도") || tokens.contains("경상도") || tokens.contains("충청도")) {
            return true;
        }

        for (RegionRecord region : regionCsvLoader.getRegionRecords()) {
            if (!"CTPRVN".equals(region.getLevel())) {
                continue;
            }

            if (matchesTopLevel(tokens, normalizedMessage, region)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasExplicitSearchableArea(String message) {
        String normalizedMessage = normalize(message);
        List<String> tokens = tokenize(message);

        if (tokens.contains("전라도") || tokens.contains("경상도") || tokens.contains("충청도")) {
            return true;
        }

        for (RegionRecord region : regionCsvLoader.getRegionRecords()) {
            if (!"CTPRVN".equals(region.getLevel()) && !"SIG".equals(region.getLevel())) {
                continue;
            }

            if ("SIG".equals(region.getLevel()) && isDistrictOnly(region.getName())) {
                continue;
            }

            if (matchesTopLevel(tokens, normalizedMessage, region)) {
                return true;
            }
        }

        return false;
    }

    private boolean isDistrictOnly(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String trimmed = value.trim();
        return !trimmed.contains(" ") && trimmed.endsWith("구");
    }

    private RegionRecord findExplicitProvince(List<String> tokens, String normalizedMessage) {
        for (RegionRecord region : regionCsvLoader.getRegionRecords()) {
            if (!"CTPRVN".equals(region.getLevel())) {
                continue;
            }

            String name = normalize(region.getName());
            String fullName = normalize(region.getFullName());

            if (StringUtils.hasText(fullName)) {
                if (tokens.contains(fullName) || isExactPhraseHit(normalizedMessage, fullName)) {
                    return region;
                }
            }

            if ("경기".equals(name) && (tokens.contains("경기도") || isExactPhraseHit(normalizedMessage, "경기도"))) {
                return region;
            }
            if ("강원".equals(name) && (tokens.contains("강원도") || isExactPhraseHit(normalizedMessage, "강원도"))) {
                return region;
            }
            if ("제주".equals(name) && (tokens.contains("제주도") || isExactPhraseHit(normalizedMessage, "제주도"))) {
                return region;
            }
            if ("충청북".equals(name) && (tokens.contains("충청북도") || isExactPhraseHit(normalizedMessage, "충청북도") || tokens.contains("충북"))) {
                return region;
            }
            if ("충청남".equals(name) && (tokens.contains("충청남도") || isExactPhraseHit(normalizedMessage, "충청남도") || tokens.contains("충남"))) {
                return region;
            }
            if ("전라북".equals(name) && (tokens.contains("전라북도") || isExactPhraseHit(normalizedMessage, "전라북도") || tokens.contains("전북"))) {
                return region;
            }
            if ("전라남".equals(name) && (tokens.contains("전라남도") || isExactPhraseHit(normalizedMessage, "전라남도") || tokens.contains("전남"))) {
                return region;
            }
            if ("경상북".equals(name) && (tokens.contains("경상북도") || isExactPhraseHit(normalizedMessage, "경상북도") || tokens.contains("경북"))) {
                return region;
            }
            if ("경상남".equals(name) && (tokens.contains("경상남도") || isExactPhraseHit(normalizedMessage, "경상남도") || tokens.contains("경남"))) {
                return region;
            }
            if ("광주".equals(name) && (tokens.contains("광주광역시") || isExactPhraseHit(normalizedMessage, "광주광역시"))) {
                return region;
            }
        }

        return null;
    }

    private String resolveBroadProvinceAlias(List<String> tokens) {
        if (tokens.contains("전라도")) return "전라도";
        if (tokens.contains("경상도")) return "경상도";
        if (tokens.contains("충청도")) return "충청도";
        return "";
    }

    private boolean isBroadProvinceAlias(String value) {
        return "전라도".equals(value) || "경상도".equals(value) || "충청도".equals(value);
    }

    private boolean matchesTopLevel(List<String> tokens, String normalizedMessage, RegionRecord region) {
        for (String candidate : buildTopLevelCandidates(region)) {
            if (!StringUtils.hasText(candidate)) {
                continue;
            }
            if (tokens.contains(candidate)) {
                return true;
            }
            if (isExactPhraseHit(normalizedMessage, candidate)) {
                return true;
            }
        }
        return false;
    }

    private List<String> buildTopLevelCandidates(RegionRecord region) {
        List<String> candidates = new ArrayList<>();

        String normalizedName = normalize(region.getName());
        String strippedName = stripSuffix(normalizedName);

        candidates.add(normalizedName);
        candidates.add(strippedName);

        if ("CTPRVN".equals(region.getLevel())) {
            addProvinceVariants(candidates, region);
        }

        if ("SIG".equals(region.getLevel())) {
            String sigHead = extractSigHead(region.getName());
            if (StringUtils.hasText(sigHead)) {
                candidates.add(normalize(sigHead));
                candidates.add(stripSuffix(normalize(sigHead)));
            }

            String sigTail = extractSigTail(region.getName(), region.getCity());
            if (StringUtils.hasText(sigTail)) {
                candidates.add(normalize(sigTail));
                candidates.add(stripSuffix(normalize(sigTail)));
            }
        }

        return dedup(candidates);
    }

    private RegionRecord findBestProvince(String normalizedMessage, List<String> tokens) {
        return findBestMatch(normalizedMessage, tokens, "CTPRVN", null, null);
    }

    private RegionRecord findBestDistrict(String normalizedMessage, List<String> tokens, String provinceName) {
        if (StringUtils.hasText(provinceName) && !isBroadProvinceAlias(provinceName)) {
            RegionRecord byProvince = findBestMatch(normalizedMessage, tokens, "SIG", provinceName, null);
            if (byProvince != null) {
                return byProvince;
            }
            return null;
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

        if (StringUtils.hasText(provinceName) && !isBroadProvinceAlias(provinceName)) {
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

            if (StringUtils.hasText(provinceName)
                    && !isBroadProvinceAlias(provinceName)
                    && !isSameAreaName(region.getCity(), provinceName)) {
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

            if (StringUtils.hasText(cityFilter)
                    && !isBroadProvinceAlias(cityFilter)
                    && !isSameAreaName(region.getCity(), cityFilter)) {
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
        for (String candidate : buildRegionCandidates(region)) {
            if (!StringUtils.hasText(candidate)) {
                continue;
            }

            if (tokens.contains(candidate)) {
                return true;
            }

            if (isExactPhraseHit(normalizedMessage, candidate)) {
                return true;
            }
        }

        return false;
    }

    private int matchScore(String normalizedMessage, List<String> tokens, RegionRecord region) {
        int score = 0;

        for (String candidate : buildRegionCandidates(region)) {
            if (!StringUtils.hasText(candidate)) {
                continue;
            }

            if (tokens.contains(candidate)) {
                score += 100 + candidate.length();
            }

            if (isExactPhraseHit(normalizedMessage, candidate)) {
                score += 20 + candidate.length();
            }
        }

        if (StringUtils.hasText(region.getParent()) && tokens.contains(normalize(region.getParent()))) {
            score += 12;
        }

        if (StringUtils.hasText(region.getCity())) {
            String normalizedCity = normalize(region.getCity());
            String strippedCity = stripSuffix(normalizedCity);

            if (tokens.contains(normalizedCity)) {
                score += 10 + normalizedCity.length();
            } else if (StringUtils.hasText(strippedCity) && tokens.contains(strippedCity)) {
                score += 8 + strippedCity.length();
            }
        }

        return score;
    }

    private List<String> buildRegionCandidates(RegionRecord region) {
        List<String> candidates = new ArrayList<>();

        String normalizedName = normalize(region.getName());
        String strippedName = stripSuffix(normalizedName);

        candidates.add(normalizedName);
        candidates.add(strippedName);

        if ("CTPRVN".equals(region.getLevel())) {
            addProvinceVariants(candidates, region);
        }

        if ("SIG".equals(region.getLevel())) {
            String sigHead = extractSigHead(region.getName());
            if (StringUtils.hasText(sigHead)) {
                candidates.add(normalize(sigHead));
                candidates.add(stripSuffix(normalize(sigHead)));
            }

            String sigTail = extractSigTail(region.getName(), region.getCity());
            if (StringUtils.hasText(sigTail)) {
                candidates.add(normalize(sigTail));
                candidates.add(stripSuffix(normalize(sigTail)));
            }
        }

        if ("EMD".equals(region.getLevel())) {
            if (StringUtils.hasText(strippedName)
                    && strippedName.length() >= 2
                    && !looksLikeBroadAmbiguousArea(strippedName)) {
                candidates.add(strippedName);
            }
        }

        return dedup(candidates);
    }

    private void addProvinceVariants(List<String> candidates, RegionRecord region) {
        String name = region.getName();
        String fullName = region.getFullName();

        if (StringUtils.hasText(fullName)) {
            candidates.add(normalize(fullName));
            candidates.add(stripSuffix(normalize(fullName)));
        }

        if (!StringUtils.hasText(name)) {
            return;
        }

        String trimmed = name.trim();

        if ("서울".equals(trimmed)) {
            candidates.add(normalize("서울시"));
            candidates.add(normalize("서울특별시"));
            return;
        }
        if ("부산".equals(trimmed)) {
            candidates.add(normalize("부산시"));
            candidates.add(normalize("부산광역시"));
            return;
        }
        if ("대구".equals(trimmed)) {
            candidates.add(normalize("대구시"));
            candidates.add(normalize("대구광역시"));
            return;
        }
        if ("인천".equals(trimmed)) {
            candidates.add(normalize("인천시"));
            candidates.add(normalize("인천광역시"));
            return;
        }
        if ("광주".equals(trimmed)) {
            candidates.add(normalize("광주광역시"));
            return;
        }
        if ("대전".equals(trimmed)) {
            candidates.add(normalize("대전시"));
            candidates.add(normalize("대전광역시"));
            return;
        }
        if ("울산".equals(trimmed)) {
            candidates.add(normalize("울산시"));
            candidates.add(normalize("울산광역시"));
            return;
        }
        if ("세종".equals(trimmed)) {
            candidates.add(normalize("세종시"));
            candidates.add(normalize("세종특별자치시"));
            return;
        }
        if ("제주".equals(trimmed)) {
            candidates.add(normalize("제주도"));
            candidates.add(normalize("제주특별자치도"));
            return;
        }
        if ("강원".equals(trimmed)) {
            candidates.add(normalize("강원도"));
            candidates.add(normalize("강원특별자치도"));
            return;
        }

        candidates.add(normalize(trimmed + "도"));

        if ("충청북".equals(trimmed)) {
            candidates.add(normalize("충북"));
            candidates.add(normalize("충청북도"));
        }
        if ("충청남".equals(trimmed)) {
            candidates.add(normalize("충남"));
            candidates.add(normalize("충청남도"));
        }
        if ("전라북".equals(trimmed)) {
            candidates.add(normalize("전북"));
            candidates.add(normalize("전라북도"));
        }
        if ("전라남".equals(trimmed)) {
            candidates.add(normalize("전남"));
            candidates.add(normalize("전라남도"));
        }
        if ("경상북".equals(trimmed)) {
            candidates.add(normalize("경북"));
            candidates.add(normalize("경상북도"));
        }
        if ("경상남".equals(trimmed)) {
            candidates.add(normalize("경남"));
            candidates.add(normalize("경상남도"));
        }
        if ("경기".equals(trimmed)) {
            candidates.add(normalize("경기도"));
        }
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

    private String extractSigTail(String sigName, String city) {
        if (!StringUtils.hasText(sigName)) {
            return "";
        }

        String[] parts = sigName.trim().split("\\s+");
        if (parts.length >= 2) {
            String last = parts[parts.length - 1];
            if (last.endsWith("시") || last.endsWith("군") || last.endsWith("구")) {
                return last;
            }
        }

        String normalizedName = normalize(sigName);
        String normalizedCity = normalize(city);

        if (StringUtils.hasText(normalizedCity)
                && normalizedName.startsWith(normalizedCity)
                && normalizedName.length() > normalizedCity.length()) {
            String tail = normalizedName.substring(normalizedCity.length()).trim();
            if (tail.endsWith("시") || tail.endsWith("군") || tail.endsWith("구")) {
                return tail;
            }
        }

        return "";
    }

    private List<String> dedup(List<String> values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }
            if (!result.contains(value)) {
                result.add(value);
            }
        }
        return result;
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
            if (!StringUtils.hasText(token)) {
                continue;
            }

            result.add(token);

            String stripped = stripKoreanParticle(token);
            if (StringUtils.hasText(stripped) && !result.contains(stripped)) {
                result.add(stripped);
            }
        }

        return result;
    }

    private String stripKoreanParticle(String token) {
        if (!StringUtils.hasText(token)) {
            return "";
        }

        return token.replaceAll("(에서|으로|로|에게서|에게|한테서|한테|부터|까지|에|을|를|은|는|이|가|와|과|랑|하고|도)$", "").trim();
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

        return value.replaceAll("(특별자치도|특별자치시|광역시|특별시|도|시|군|구|동|읍|면|리)$", "").trim();
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase()
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        normalized = normalized
                .replaceAll("(맛집|음식점|식당|밥집|카페|디저트|베이커리|빵집)", " $1")
                .replaceAll("(숙소|숙박|호텔|모텔|펜션|리조트|게스트하우스|호스텔|민박|풀빌라|한옥스테이)", " $1")
                .replaceAll("(명소|관광지|볼거리|랜드마크|핫플|핫플레이스)", " $1")
                .replaceAll("(일정|코스|여행|플랜|동선|루트)", " $1")
                .replaceAll("(국제공항|공항)", " 공항")
                .replaceAll("(터미널)", " 터미널")
                .replaceAll("(역)", " 역");

        return normalized.replaceAll("\\s+", " ").trim();
    }

    public static class ResolvedRegion {
        private final String province;
        private final String city;
        private final String district;
        private final String neighborhood;
        private final String detailName;

        public ResolvedRegion(String province, String city, String district, String neighborhood, String detailName) {
            this.province = province;
            this.city = city;
            this.district = district;
            this.neighborhood = neighborhood;
            this.detailName = detailName;
        }

        public String getProvince() {
            return province;
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