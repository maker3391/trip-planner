package com.fiveguys.trip_planner.response;

import com.fiveguys.trip_planner.entity.TripPlan;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
public class TripPlanResponseDto {
    private Long id;
    private Long ownerId;
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime createdAt;

    public TripPlanResponseDto(TripPlan tripPlan) {
        this.id = tripPlan.getId();
        this.ownerId = tripPlan.getOwner().getId();
        this.title = tripPlan.getTitle();
        this.destination = tripPlan.getDestination();
        this.startDate = tripPlan.getStartDate();
        this.endDate = tripPlan.getEndDate();
        this.status = tripPlan.getStatus();
        this.createdAt = tripPlan.getCreatedAt();
    }
}
