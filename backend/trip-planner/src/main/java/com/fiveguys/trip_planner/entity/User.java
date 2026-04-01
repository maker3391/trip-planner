package com.fiveguys.trip_planner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 30)
    private String provider;

    @Column(length = 100)
    private String providerId;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static User createUser(String email, String encodedPassword, String name, String phone) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setName(name);
        user.setPhone(phone);
        user.setRole("USER");
        user.setStatus("ACTIVE");
        return user;
    }

    public static User createOAuthUser(String email, String name, String provider, String providerId) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("OAUTH2_TEMP_PASSWORD");
        user.setName(name);
        user.setPhone(null);
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setProvider(provider);
        user.setProviderId(providerId);
        return user;
    }
}