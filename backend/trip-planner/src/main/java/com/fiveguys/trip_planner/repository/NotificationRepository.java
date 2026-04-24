package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.Notification;
import com.fiveguys.trip_planner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByReceiverAndIsReadFalseOrderByCreatedAtDesc(User receiver);
}
