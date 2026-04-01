package com.fiveguys.trip_planner.dto.response;

import com.fiveguys.trip_planner.entity.Place;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
public class PlaceResponseDto {
    private Long id;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String category;
    private String externalPlaceId;
    private String placeUrl;
    private LocalDateTime createdAt;

    public PlaceResponseDto(Place place) {
        this.id = place.getId();
        this.name = place.getName();
        this.address = place.getAddress();
        this.latitude = place.getLatitude();
        this.longitude = place.getLongitude();
        this.category = place.getCategory();
        this.externalPlaceId = place.getExternalPlaceId();
        this.placeUrl = place.getPlaceUrl();
        this.createdAt = place.getCreatedAt();
    }
}
