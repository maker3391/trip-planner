package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    // ✅ 카테고리별 조회 (페이징)
    Page<Community> findByCategory(String category, Pageable pageable);

    // ✅ 지역별 조회
    Page<Community> findByRegion(String region, Pageable pageable);

    // ✅ 카테고리 + 지역
    Page<Community> findByCategoryAndRegion(String category, String region, Pageable pageable);

    Page<Community> findByTitle(String title, Pageable pageable);

    // ✅ 제목 검색
    Page<Community> findByTitleContaining(String keyword, Pageable pageable);

    // ✅ 제목 + 내용 검색 (추천)
    Page<Community> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
}