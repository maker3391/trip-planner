package com.fiveguys.trip_planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
//프론트 -> 백
@Getter @Setter
@NoArgsConstructor
public class TripPlanRequestDto {
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    private List<TripScheduleRequestDto> schedules;
}
