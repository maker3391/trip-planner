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

    // 기존에 TripDay로 연결되어있어서 TripPlan으로 연결 방향 전환(며칠 차 직접 입력한다고 해서 수정)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id", nullable = false)
    private TripPlan tripPlan;
    // 직접 입력한 일 차 저장할 필드
    @Column(nullable = false)
    private Integer dayNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer visitOrder;

    @Column(name = "pin_color", length = 20)
    private String pinColor;

    @Column(name = "selected_pin_color", length = 20)
    private String selectedPinColor;

    @Column(name = "line_color", length = 20)
    private String lineColor;

    private LocalTime startTime;
    private LocalTime endTime;
    private String memo;
    private Integer estimatedStayMinutes;
}