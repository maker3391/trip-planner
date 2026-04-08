package com.fiveguys.trip_planner.dto;

public class RegionAliasRecord {

    private final String alias;
    private final String city;
    private final String targetLevel;
    private final String targetName;
    private final String targetParent;
    private final String queryHint;
    private final int priority;

    public RegionAliasRecord(String alias,
                             String city,
                             String targetLevel,
                             String targetName,
                             String targetParent,
                             String queryHint,
                             int priority) {
        this.alias = alias;
        this.city = city;
        this.targetLevel = targetLevel;
        this.targetName = targetName;
        this.targetParent = targetParent;
        this.queryHint = queryHint;
        this.priority = priority;
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

    public int getPriority() {
        return priority;
    }
}