package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.PlaceResponseDto;
import com.fiveguys.trip_planner.service.PlaceService;
import com.fiveguys.trip_planner.dto.PlaceRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "📍 장소 관리 API", description = "시스템 내부에 저장되는 장소(Place) 정보를 관리합니다.")
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    @Operation(
            summary = "새로운 장소 등록",
            description = "구글 맵에서 가져온 장소 정보(이름, 주소, 좌표 등)를 우리 시스템 DB에 정식으로 저장합니다."
    )
    @PostMapping
    public ResponseEntity<PlaceResponseDto> createPlace(@RequestBody PlaceRequestDto requestDto) {
        PlaceResponseDto responseDto = placeService.createPlace(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
