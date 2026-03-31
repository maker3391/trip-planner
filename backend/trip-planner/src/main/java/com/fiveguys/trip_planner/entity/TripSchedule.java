package com.fiveguys.trip_planner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "trip_schedules")
@Getter @Setter
@NoArgsConstructor
public class TripSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_day_id", nullable = false)
    private TripDay tripDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer visitOrder;

    private LocalTime startTime;
    private LocalTime endTime;
    private String memo;
    private Integer estimatedStayMinutes;
}