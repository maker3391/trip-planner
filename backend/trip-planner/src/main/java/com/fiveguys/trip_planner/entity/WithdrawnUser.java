package com.fiveguys.trip_planner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "withdraw_user")
@Getter
@Setter
@NoArgsConstructor
public class WithdrawnUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 30)
    private String nickName;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false)
    private LocalDateTime withdrawAt;

    public WithdrawnUser(String email, String nickName, String phone) {
        this.email = email;
        this.nickName = nickName;
        this.phone = phone;
        this.withdrawAt = LocalDateTime.now();
    }
}
