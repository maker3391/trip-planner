package com.fiveguys.trip_planner.response;

import lombok.Data;


import java.util.List;

@Data
public class GooglePlaceResponse {
    private List<Candidate> places;

    @Data
    public static class Candidate {
        private String id;
        private String formattedAddress;
        private DisplayName displayName;
        private Location location;

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
}