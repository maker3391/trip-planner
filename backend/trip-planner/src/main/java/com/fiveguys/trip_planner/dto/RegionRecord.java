package com.fiveguys.trip_planner.dto;

public class RegionRecord {

    private final String code;
    private final String name;
    private final String parent;
    private final String city;
    private final String level;
    private final String fullName;
    private final String source;

    public RegionRecord(String code, String name, String parent, String city,
                        String level, String fullName, String source) {
        this.code = code;
        this.name = name;
        this.parent = parent;
        this.city = city;
        this.level = level;
        this.fullName = fullName;
        this.source = source;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public String getCity() {
        return city;
    }

    public String getLevel() {
        return level;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSource() {
        return source;
    }
}