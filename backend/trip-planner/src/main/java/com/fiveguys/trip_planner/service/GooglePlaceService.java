package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.client.GooglePlaceClient;
import com.fiveguys.trip_planner.response.GooglePlaceResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GooglePlaceService {
    private final GooglePlaceClient googlePlaceClient;

    public List<GooglePlaceResponse.Candidate> search(String keyword) {
        GooglePlaceResponse response = googlePlaceClient.searchPlace(keyword);

        if(response == null || response.getCandidates() == null) {
            return List.of();
        }

        return response.getCandidates();
    }
}
