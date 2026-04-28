package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.ChatRoom;
import com.fiveguys.trip_planner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByUserOrderByCreatedAtDesc(User user);

    List<ChatRoom> findByUserAndDeletedByUserFalseOrderByCreatedAtDesc(User user);
}
