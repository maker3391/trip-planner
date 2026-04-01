package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.response.GooglePlaceResponse;
import com.fiveguys.trip_planner.service.GooglePlaceService;
import com.fiveguys.trip_planner.service.PlaceService;
import com.fiveguys.trip_planner.dto.PlaceRequestDto;
import com.fiveguys.trip_planner.response.PlaceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {
    private final GooglePlaceService googlePlaceService;
    private final PlaceService placeService;

    @GetMapping("/search")
    public List<GooglePlaceResponse.Candidate> searchPlaces(@RequestParam String keyword) {
        return googlePlaceService.search(keyword);
    }

    @PostMapping
    public ResponseEntity<PlaceResponseDto> createPlace(@RequestBody PlaceRequestDto requestDto) {
        PlaceResponseDto responseDto = placeService.createPlace(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
