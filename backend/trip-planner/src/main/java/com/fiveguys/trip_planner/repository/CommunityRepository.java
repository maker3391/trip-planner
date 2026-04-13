package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    /**
     * 🔥 커뮤니티 통합 필터링 조회 (동적 쿼리)
     * 카테고리, 지역, 검색 조건에 따라 결과를 반환합니다.
     */
    @Query("SELECT c FROM Community c WHERE " +
            "(:category IS NULL OR c.category = :category) AND " +
            "(:region IS NULL OR c.region = :region) AND " +
            "(" +
            "  :keyword IS NULL OR " +
            "  (:searchType = 'title' AND c.title LIKE :keyword) OR " +
            "  (:searchType = 'author' AND c.authorNickname LIKE :keyword)" +
            ")")
    Page<Community> findWithFilters(
            @Param("category") String category,
            @Param("region") String region,
            @Param("searchType") String searchType,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // --- 아래는 특정 상황에서 단독으로 사용할 수 있는 기존 메서드들입니다 ---

    // 카테고리별 조회
    Page<Community> findByCategory(String category, Pageable pageable);

    // 지역별 조회
    Page<Community> findByRegion(String region, Pageable pageable);

    // 제목 검색
    Page<Community> findByTitleContaining(String keyword, Pageable pageable);

    // 작성자 기준 검색
    Page<Community> findByAuthorNickname(String authorNickname, Pageable pageable);

    // 조회수 기준 내림차순
    Page<Community> findByOrderByViewCountDesc(Pageable pageable);

    // 추천수 기준 내림차순
    Page<Community> findByOrderByRecommendCountDesc(Pageable pageable);
}