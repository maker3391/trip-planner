package com.fiveguys.trip_planner.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


import java.util.List;

@Schema(description = "구글 장소 검색 결과 응답 객체")
@Data
public class GooglePlaceResponse {

    @Schema(description = "검색된 장소 후보지 리스트")
    private List<Candidate> places;

    @Data
    @Schema(description = "개별 장소 정보 (후보지)")
    public static class Candidate {

        @Schema(description = "Google Place ID (고유 식별자)", example = "ChIJabcdEFGH1234")
        private String id;

        @Schema(description = "장소의 전체 주소", example = "부산광역시 해운대구 우동 123-4")
        private String formattedAddress;

        @Schema(description = "장소 표시 이름 정보")
        private DisplayName displayName;

        @Schema(description = "장소의 지리적 위치 좌표")
        private Location location;

        @Data
        @Schema(description = "장소 명칭 정보")
        public static class DisplayName {

            @Schema(description = "장소 이름", example = "해운대 해수욕장")
            private String text;
        }

        @Data
        @Schema(description = "위도 및 경도 정보")
        public static class Location {

            @Schema(description = "위도 (Latitude)", example = "35.1587")
            private Double latitude;

            @Schema(description = "경도 (Longitude)", example = "129.1604")
            private Double longitude;
        }
    }
}