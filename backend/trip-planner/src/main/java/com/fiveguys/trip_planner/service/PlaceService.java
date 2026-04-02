package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.PlaceRequestDto;
import com.fiveguys.trip_planner.dto.PlaceResponseDto;

import com.fiveguys.trip_planner.entity.Place;
import com.fiveguys.trip_planner.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final PlaceRepository placeRepository;

    @Transactional
    public PlaceResponseDto createPlace(PlaceRequestDto requestDto) {
        Place place = new Place();
        place.setName(requestDto.getName());
        place.setAddress(requestDto.getAddress());
        place.setLatitude(requestDto.getLatitude());
        place.setLongitude(requestDto.getLongitude());
        place.setCategory(requestDto.getCategory());
        place.setExternalPlaceId(requestDto.getExternalPlaceId());
        place.setPlaceUrl(requestDto.getPlaceUrl());

        Place savePlace = placeRepository.save(place);

        return new PlaceResponseDto(savePlace);
    }
}
