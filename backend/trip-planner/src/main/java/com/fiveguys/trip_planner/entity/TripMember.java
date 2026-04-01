package com.fiveguys.trip_planner.entity;

import com.fiveguys.trip_planner.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip_members")
@Getter @Setter
@EntityListeners(AuditingEntityListener.class) // Auditing 기능을 포함시킴
@NoArgsConstructor
public class TripMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id", nullable = false)
    private TripPlan tripPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String role;

    @CreatedDate // Entity가 생성되어 저장될 때 시간이 자동 저장됨
    @Column(updatable = false)
    private LocalDateTime createdAt;
}