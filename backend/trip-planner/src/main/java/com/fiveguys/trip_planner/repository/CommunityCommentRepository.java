package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.Community;
import com.fiveguys.trip_planner.entity.CommunityComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    Page<CommunityComment> findByCommunityAndParentIsNull(
            Community community,
            Pageable pageable
    );

    List<CommunityComment> findByParentIn(List<CommunityComment> parents);
}