package com.fiveguys.trip_planner.config;

import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if(userRepository.findByEmail("admin@admin.com").isEmpty()) {
            User admin = new User();

            admin.setEmail("admin@admin.com");
            admin.setPassword(passwordEncoder.encode("admin"));

            admin.setName("관리자");
            admin.setNickname("관리자");
            admin.setPhone("010-0000-0000");
            admin.setStatus("ACTIVE");
            admin.setRole("ADMIN");

            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            userRepository.save(admin);
        }
    }
}
