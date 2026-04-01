package com.fiveguys.trip_planner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


import java.util.List;

@Data
public class GooglePlaceResponse {
    private List<Candidate> places;

    private String status;

    @Data
    public static class Candidate {
        private String id;
        private String formattedAddress;
        private DisplayName diplayName;
        private Location location;
    }

    @Data
    public static class DisplayName {
        private String text;
    }

    @Data
    public static class Location {
        private Double latitude;
        private Double longitude;
    }
}
