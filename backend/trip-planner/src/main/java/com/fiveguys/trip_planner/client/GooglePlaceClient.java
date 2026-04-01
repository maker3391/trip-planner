package com.fiveguys.trip_planner.client;

<<<<<<< HEAD
import com.fiveguys.trip_planner.dto.GooglePlaceResponse;
=======
import com.fiveguys.trip_planner.dto.response.GooglePlaceResponse;
>>>>>>> 839db98d3a8c55ff3a2c7b51732a7fc635ee03a0

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GooglePlaceClient {
    private final RestTemplate restTemplate;

    @Value("${google.api.key}")
    private String apiKey;

    private final String GOOGLE_PLACE_URL =
            "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input={input}&inputtype=textquery&fields={fields}&key={key}";

    public GooglePlaceResponse searchPlace(String query) {

        String fields = "formatted_address,name,geometry,place_id";

        return restTemplate.getForObject(
                GOOGLE_PLACE_URL,
                GooglePlaceResponse.class,
                query,
                fields,
                apiKey
        );
    }
}