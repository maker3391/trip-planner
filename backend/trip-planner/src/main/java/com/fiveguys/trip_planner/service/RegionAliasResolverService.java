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
        String normalizedCity = normalize(resolvedCity);

        List<RegionAliasRecord> candidates = regionAliasCsvLoader.getAliasRecords().stream()
                .filter(record -> StringUtils.hasText(record.getAlias()))
                .filter(record -> normalizedMessage.contains(normalize(record.getAlias())))
                .filter(record -> {
                    if (!StringUtils.hasText(record.getCity())) {
                        return true;
                    }
                    if (!StringUtils.hasText(normalizedCity)) {
                        return true;
                    }
                    return normalize(record.getCity()).equals(normalizedCity);
                })
                .sorted(Comparator
                        .comparingInt(RegionAliasRecord::getPriority).reversed()
                        .thenComparingInt((RegionAliasRecord record) -> record.getAlias().length()).reversed())
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

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "")
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