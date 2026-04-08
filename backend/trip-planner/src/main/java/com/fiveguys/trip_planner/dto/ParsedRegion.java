package com.fiveguys.trip_planner.dto;

public class ParsedRegion {

    private final String province;
    private final String city;
    private final String district;
    private final String neighborhood;
    private final RegionScope scope;
    private final String displayDestination;

    public ParsedRegion(String province,
                        String city,
                        String district,
                        String neighborhood,
                        RegionScope scope,
                        String displayDestination) {
        this.province = province;
        this.city = city;
        this.district = district;
        this.neighborhood = neighborhood;
        this.scope = scope;
        this.displayDestination = displayDestination;
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

    public RegionScope getScope() {
        return scope;
    }

    public String getDisplayDestination() {
        return displayDestination;
    }
}