package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
