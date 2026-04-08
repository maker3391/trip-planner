package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.response.GooglePlaceResponse;
import com.fiveguys.trip_planner.service.GooglePlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "🔍 장소 검색 API", description = "구글 맵 API를 사용하여 장소 키워드 검색 및 위치 정보를 제공합니다.")
@RestController
@RequestMapping("/api/google-places")
@RequiredArgsConstructor
public class GooglePlaceController {
    private final GooglePlaceService googlePlaceService;

    @Operation(
            summary = "장소 키워드 검색",
            description = "사용자가 입력한 키워드(예: '해운대 맛집')를 바탕으로 구글 장소 데이터에서 위도, 경도, 주소 등 후보지 리스트를 가져옵니다."
    )
    @GetMapping("/search")
    public List<GooglePlaceResponse.Candidate> searchPlace(@RequestParam String keyword) {
        return googlePlaceService.search(keyword);
    }
}
