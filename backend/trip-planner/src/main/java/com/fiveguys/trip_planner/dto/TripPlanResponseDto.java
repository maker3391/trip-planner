package com.fiveguys.trip_planner.dto;

import com.fiveguys.trip_planner.entity.TripPlan;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
//백 -> 프론트
@Getter
public class TripPlanResponseDto {
    private final Long id;
    private final Long ownerId;
    private final String title;
    private final String destination;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String status;
    private final LocalDateTime createdAt;

    private List<TripScheduleResponseDto> schedules;

    public TripPlanResponseDto(TripPlan tripPlan) {
        this.id = tripPlan.getId();
        this.ownerId = tripPlan.getOwner().getId();
        this.title = tripPlan.getTitle();
        this.destination = tripPlan.getDestination();
        this.startDate = tripPlan.getStartDate();
        this.endDate = tripPlan.getEndDate();
        this.status = tripPlan.getStatus();
        this.createdAt = tripPlan.getCreatedAt();

        if (tripPlan.getSchedules() != null) {
            this.schedules = tripPlan.getSchedules().stream()
                    .map(TripScheduleResponseDto::new)
                    .collect(Collectors.toList());
        }
    }
}
