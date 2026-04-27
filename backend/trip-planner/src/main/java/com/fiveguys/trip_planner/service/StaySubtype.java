package com.fiveguys.trip_planner.service;

public enum StaySubtype {
    GENERIC("generic"),
    HOTEL("hotel"),
    MOTEL("motel"),
    PENSION("pension"),
    RESORT("resort"),
    GUEST_HOUSE("guesthouse"),
    HANOK("hanok"),
    POOL_VILLA("poolvilla"),
    OCEAN_VIEW("oceanview"),
    EMOTIONAL("emotional"),
    BUDGET("budget");

    private final String value;

    StaySubtype(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static StaySubtype from(String value) {
        if (value == null) {
            return GENERIC;
        }

        for (StaySubtype subtype : values()) {
            if (subtype.value.equals(value)) {
                return subtype;
            }
        }

        return GENERIC;
    }
}