package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhone(String phone);

    boolean existsByNicknameAndIdNot(String nickname, Long id);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}