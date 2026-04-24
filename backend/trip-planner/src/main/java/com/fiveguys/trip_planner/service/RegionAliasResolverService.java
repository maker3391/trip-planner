package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.RegionAliasRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RegionAliasResolverService {

    private final RegionAliasCsvLoader regionAliasCsvLoader;

    public RegionAliasResolverService(RegionAliasCsvLoader regionAliasCsvLoader) {
        this.regionAliasCsvLoader = regionAliasCsvLoader;
    }

    public ResolvedAlias resolve(String message, String resolvedCity) {
        String normalizedMessage = normalize(message);
        List<String> tokens = tokenize(message);
        String normalizedCity = normalize(resolvedCity);

        List<AliasCandidate> candidates = new ArrayList<>();

        for (RegionAliasRecord record : regionAliasCsvLoader.getAliasRecords()) {
            if (!StringUtils.hasText(record.getAlias())) {
                continue;
            }

            if (!matchesAlias(normalizedMessage, tokens, record.getAlias())) {
                continue;
            }

            if (!isCityMatched(record.getCity(), normalizedCity)) {
                continue;
            }

            int specificity = computeSpecificityScore(record, normalizedMessage, tokens);
            candidates.add(new AliasCandidate(record, specificity));
        }

        if (candidates.isEmpty()) {
            return null;
        }

        AliasCandidate best = null;
        for (AliasCandidate candidate : candidates) {
            if (best == null || compare(candidate, best) > 0) {
                best = candidate;
            }
        }

        RegionAliasRecord selected = best.record();

        return new ResolvedAlias(
                selected.getAlias(),
                selected.getCity(),
                selected.getTargetLevel(),
                selected.getTargetName(),
                selected.getTargetParent(),
                selected.getQueryHint()
        );
    }

    private int compare(AliasCandidate a, AliasCandidate b) {
        if (a.specificityScore() != b.specificityScore()) {
            return Integer.compare(a.specificityScore(), b.specificityScore());
        }

        if (a.record().getPriority() != b.record().getPriority()) {
            return Integer.compare(a.record().getPriority(), b.record().getPriority());
        }

        int aAliasLen = safeLength(a.record().getAlias());
        int bAliasLen = safeLength(b.record().getAlias());
        if (aAliasLen != bAliasLen) {
            return Integer.compare(aAliasLen, bAliasLen);
        }

        int aTargetLen = safeLength(a.record().getTargetName());
        int bTargetLen = safeLength(b.record().getTargetName());
        if (aTargetLen != bTargetLen) {
            return Integer.compare(aTargetLen, bTargetLen);
        }

        int aParentLen = safeLength(a.record().getTargetParent());
        int bParentLen = safeLength(b.record().getTargetParent());
        return Integer.compare(aParentLen, bParentLen);
    }

    private int computeSpecificityScore(RegionAliasRecord record,
                                        String normalizedMessage,
                                        List<String> tokens) {
        int score = 0;

        String alias = normalize(record.getAlias());
        String city = normalize(record.getCity());
        String targetName = normalize(record.getTargetName());
        String targetParent = normalize(record.getTargetParent());
        String hint = normalize(record.getQueryHint());

        if (tokens.contains(alias)) {
            score += 100;
        } else if (containsTokenPrefix(tokens, alias)) {
            score += 80;
        } else if (containsPhrase(normalizedMessage, alias)) {
            score += 60;
        }

        if (StringUtils.hasText(targetName)) {
            score += 25;
        }

        if (StringUtils.hasText(targetParent)) {
            score += 15;
        }

        if (StringUtils.hasText(hint)) {
            score += 10;
        }

        if (StringUtils.hasText(city)) {
            score += 5;
        }

        score += safeLength(record.getAlias()) * 3;
        score += safeLength(record.getTargetName()) * 2;
        score += safeLength(record.getTargetParent());

        if (isBroadAlias(record)) {
            score -= 30;
        }

        return score;
    }

    private boolean isBroadAlias(RegionAliasRecord record) {
        String city = normalize(record.getCity());
        String targetName = normalize(record.getTargetName());
        String hint = normalize(record.getQueryHint());

        return StringUtils.hasText(city)
                && city.equals(targetName)
                && city.equals(hint);
    }

    private boolean matchesAlias(String normalizedMessage, List<String> tokens, String alias) {
        String normalizedAlias = normalize(alias);

        if (!StringUtils.hasText(normalizedAlias)) {
            return false;
        }

        if (tokens.contains(normalizedAlias)) {
            return true;
        }

        if (containsTokenPrefix(tokens, normalizedAlias)) {
            return true;
        }

        return containsPhrase(normalizedMessage, normalizedAlias);
    }

    private boolean containsTokenPrefix(List<String> tokens, String alias) {
        for (String token : tokens) {
            if (alias.length() >= 2 && token.startsWith(alias)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsPhrase(String normalizedMessage, String alias) {
        String paddedMessage = " " + normalizedMessage + " ";
        String paddedAlias = " " + alias + " ";
        return paddedMessage.contains(paddedAlias);
    }

    private boolean isCityMatched(String aliasCity, String resolvedCity) {
        if (!StringUtils.hasText(aliasCity)) {
            return true;
        }

        if (!StringUtils.hasText(resolvedCity)) {
            return false;
        }

        String normalizedAliasCity = normalize(aliasCity);
        String normalizedResolvedCity = normalize(resolvedCity);

        if (normalizedAliasCity.equals(normalizedResolvedCity)) {
            return true;
        }

        boolean aliasEndsWithSig = endsWithSigUnit(aliasCity);
        boolean resolvedEndsWithSig = endsWithSigUnit(resolvedCity);

        if (aliasEndsWithSig != resolvedEndsWithSig) {
            return false;
        }

        String strippedAliasCity = stripSuffix(normalizedAliasCity);
        String strippedResolvedCity = stripSuffix(normalizedResolvedCity);

        return StringUtils.hasText(strippedAliasCity)
                && StringUtils.hasText(strippedResolvedCity)
                && strippedAliasCity.equals(strippedResolvedCity);
    }

    private boolean endsWithSigUnit(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String trimmed = value.trim();
        return trimmed.endsWith("시") || trimmed.endsWith("군") || trimmed.endsWith("구");
    }

    private List<String> tokenize(String value) {
        String normalized = normalize(value);
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

    private int safeLength(String value) {
        return value == null ? 0 : normalize(value).length();
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

    private record AliasCandidate(RegionAliasRecord record, int specificityScore) {
    }

    public static class ResolvedAlias {
        private final String alias;
        private final String city;
        private final String targetLevel;
        private final String targetName;
        private final String targetParent;
        private final String queryHint;

        public ResolvedAlias(String alias,
                             String city,
                             String targetLevel,
                             String targetName,
                             String targetParent,
                             String queryHint) {
            this.alias = alias;
            this.city = city;
            this.targetLevel = targetLevel;
            this.targetName = targetName;
            this.targetParent = targetParent;
            this.queryHint = queryHint;
        }

        public String getAlias() {
            return alias;
        }

        public String getCity() {
            return city;
        }

        public String getTargetLevel() {
            return targetLevel;
        }

        public String getTargetName() {
            return targetName;
        }

        public String getTargetParent() {
            return targetParent;
        }

        public String getQueryHint() {
            return queryHint;
        }
    }
}