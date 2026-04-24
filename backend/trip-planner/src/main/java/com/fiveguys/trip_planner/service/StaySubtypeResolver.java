package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;

@Service
public class StaySubtypeResolver {

    public StaySubtype resolve(String message, String intent) {
        if (!"STAY_RECOMMENDATION".equals(intent)) {
            return StaySubtype.GENERIC;
        }

        String value = message == null ? "" : message.toLowerCase();

        if (value.contains("풀빌라")) {
            return StaySubtype.POOL_VILLA;
        }

        if (value.contains("한옥스테이") || value.contains("한옥 숙소") || value.contains("한옥")) {
            return StaySubtype.HANOK;
        }

        if (value.contains("게스트하우스")) {
            return StaySubtype.GUEST_HOUSE;
        }

        if (value.contains("리조트")) {
            return StaySubtype.RESORT;
        }

        if (value.contains("펜션")) {
            return StaySubtype.PENSION;
        }

        if (value.contains("무인텔") || value.contains("모텔")) {
            return StaySubtype.MOTEL;
        }

        if (value.contains("호텔")) {
            return StaySubtype.HOTEL;
        }

        return StaySubtype.GENERIC;
    }
}