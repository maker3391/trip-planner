package com.fiveguys.trip_planner.dto;

import java.util.List;
import java.util.stream.Collectors;

public class ResolvedRegion {

    private final String originalInput;
    private final String normalizedInput;
    private final List<RegionTarget> targets;
    private final boolean sigunguScoped;
    private final String requestedSigunguName;

    public ResolvedRegion(String originalInput,
                          String normalizedInput,
                          List<RegionTarget> targets,
                          boolean sigunguScoped,
                          String requestedSigunguName) {
        this.originalInput = originalInput;
        this.normalizedInput = normalizedInput;
        this.targets = targets;
        this.sigunguScoped = sigunguScoped;
        this.requestedSigunguName = requestedSigunguName;
    }

    public String getOriginalInput() {
        return originalInput;
    }

    public String getNormalizedInput() {
        return normalizedInput;
    }

    public List<RegionTarget> getTargets() {
        return targets;
    }

    public boolean isSigunguScoped() {
        return sigunguScoped;
    }

    public String getRequestedSigunguName() {
        return requestedSigunguName;
    }

    public List<String> getAreaCodes() {
        return targets.stream()
                .map(RegionTarget::getAreaCode)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getSigunguCodes() {
        return targets.stream()
                .map(RegionTarget::getSigunguCode)
                .collect(Collectors.toList());
    }

    public String getPrimaryAreaName() {
        if (targets == null || targets.isEmpty()) {
            return null;
        }
        return targets.get(0).getAreaName();
    }

    public String getPrimaryAreaCode() {
        if (targets == null || targets.isEmpty()) {
            return null;
        }
        return targets.get(0).getAreaCode();
    }
}