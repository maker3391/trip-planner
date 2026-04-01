package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.GooglePlaceResponse;
import com.fiveguys.trip_planner.service.GooglePlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/google-places")
@RequiredArgsConstructor
public class GooglePlaceController {
    private final GooglePlaceService googlePlaceService;

    @GetMapping("/search")
    public List<GooglePlaceResponse.Candidate> searchPlace(@RequestParam String keyword) {
        return googlePlaceService.search(keyword);
    }
}
