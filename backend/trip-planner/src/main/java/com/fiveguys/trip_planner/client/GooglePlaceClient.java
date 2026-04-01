package com.fiveguys.trip_planner.client;


import com.fiveguys.trip_planner.dto.GooglePlaceResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GooglePlaceClient {
    private final RestTemplate restTemplate;

    @Value("${google.api.key}")
    private String apiKey;

    private final String GOOGLE_PLACE_URL =
            "https://places.googleapis.com/v1/places:searchText";

    public GooglePlaceResponse searchPlace(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", apiKey);
        headers.set("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.location");

        Map<String, String> body = Map.of("textQuery", query);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(
                GOOGLE_PLACE_URL,
                entity,
                GooglePlaceResponse.class
        );
    }
}