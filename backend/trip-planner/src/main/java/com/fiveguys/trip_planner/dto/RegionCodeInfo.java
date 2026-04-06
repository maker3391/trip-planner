package com.fiveguys.trip_planner.dto;

public class RegionCodeInfo {

    private String destination;
    private String areaCode;
    private String sigunguCode;

    public RegionCodeInfo() {
    }

    public RegionCodeInfo(String destination, String areaCode, String sigunguCode) {
        this.destination = destination;
        this.areaCode = areaCode;
        this.sigunguCode = sigunguCode;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getSigunguCode() {
        return sigunguCode;
    }

    public void setSigunguCode(String sigunguCode) {
        this.sigunguCode = sigunguCode;
    }
}