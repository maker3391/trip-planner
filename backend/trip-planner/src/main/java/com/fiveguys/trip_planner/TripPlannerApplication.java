package com.fiveguys.trip_planner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TripPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripPlannerApplication.class, args);
	}

}
