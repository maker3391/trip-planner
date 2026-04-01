package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.service.PlaceService;
import com.fiveguys.trip_planner.dto.PlaceRequestDto;
import com.fiveguys.trip_planner.dto.PlaceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    @PostMapping
    public ResponseEntity<PlaceResponseDto> createPlace(@RequestBody PlaceRequestDto requestDto) {
        PlaceResponseDto responseDto = placeService.createPlace(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
