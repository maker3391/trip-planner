package com.fiveguys.trip_planner.dto.response;

import lombok.Data;


import java.util.List;

@Data
public class GooglePlaceResponse {
    private List<Candidate> candidates;
    private String status;

    @Data
    public static class Candidate {
        private String name;
        private String formatted_address;
        private Geometry geometry;
        private String place_id;
    }

    @Data
    public static class Geometry {
        private Location location;
    }

    @Data
    public static class Location {
        private Double lat;
        private Double lng;
    }
}
