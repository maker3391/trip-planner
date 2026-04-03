package com.fiveguys.trip_planner.dto;

public class RegionTarget {

    private final String areaName;
    private final String areaCode;
    private final String sigunguName;
    private final String sigunguCode;

    public RegionTarget(String areaName, String areaCode, String sigunguName, String sigunguCode) {
        this.areaName = areaName;
        this.areaCode = areaCode;
        this.sigunguName = sigunguName;
        this.sigunguCode = sigunguCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public String getSigunguName() {
        return sigunguName;
    }

    public String getSigunguCode() {
        return sigunguCode;
    }
}