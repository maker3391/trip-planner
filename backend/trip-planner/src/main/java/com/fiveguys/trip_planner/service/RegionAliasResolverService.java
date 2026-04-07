package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.RegionAliasRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Comparator;
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

        List<RegionAliasRecord> candidates = regionAliasCsvLoader.getAliasRecords().stream()
                .filter(record -> StringUtils.hasText(record.getAlias()))
                .filter(record -> matchesAlias(normalizedMessage, tokens, record.getAlias()))
                .filter(record -> isCityMatched(record, normalizedCity))
                .sorted(Comparator
                        .comparingInt(RegionAliasRecord::getPriority).reversed()
                        .thenComparingInt((RegionAliasRecord record) -> normalize(record.getAlias()).length()).reversed())
                .toList();

        if (candidates.isEmpty()) {
            return null;
        }

        RegionAliasRecord best = candidates.get(0);

        return new ResolvedAlias(
                best.getAlias(),
                best.getCity(),
                best.getTargetLevel(),
                best.getTargetName(),
                best.getTargetParent(),
                best.getQueryHint()
        );
    }

    private boolean isCityMatched(RegionAliasRecord record, String normalizedCity) {
        if (!StringUtils.hasText(record.getCity())) {
            return true;
        }

        if (!StringUtils.hasText(normalizedCity)) {
            return false;
        }

        return normalize(record.getCity()).equals(normalizedCity);
    }

    private boolean matchesAlias(String normalizedMessage, List<String> tokens, String alias) {
        String normalizedAlias = normalize(alias);

        if (tokens.contains(normalizedAlias)) {
            return true;
        }

        String paddedMessage = " " + normalizedMessage + " ";
        String paddedAlias = " " + normalizedAlias + " ";

        return paddedMessage.contains(paddedAlias);
    }

    private List<String> tokenize(String value) {
        String normalized = normalize(value);
        if (!StringUtils.hasText(normalized)) {
            return List.of();
        }
        return List.of(normalized.split("\\s+"));
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