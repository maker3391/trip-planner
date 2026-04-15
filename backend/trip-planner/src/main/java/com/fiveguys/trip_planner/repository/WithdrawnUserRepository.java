package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.WithdrawnUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface WithdrawnUserRepository extends JpaRepository<WithdrawnUser, Long> {
    boolean existsByEmailAndWithdrawnAtAfter(String email, LocalDateTime date);
    boolean existsByNicknameAndWithdrawnAtAfter(String nickname, LocalDateTime date);
    boolean existsByPhoneAndWithdrawnAtAfter(String phone, LocalDateTime date);
}
