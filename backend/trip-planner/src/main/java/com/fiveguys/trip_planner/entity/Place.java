package com.fiveguys.trip_planner.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "places")
@Getter @Setter
@EntityListeners(AuditingEntityListener.class) // Auditing 기능을 포함시킴
@NoArgsConstructor
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    private String category;

    @Column(name = "external_place_id")
    private String externalPlaceId;

    @Column(name = "place_url", length = 1000)
    private String placeUrl;

    @CreatedDate // Entity가 생성되어 저장될 때 시간이 자동 저장됨
    @Column(updatable = false)
    private LocalDateTime createdAt;
}