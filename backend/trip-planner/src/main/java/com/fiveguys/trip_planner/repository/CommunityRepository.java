package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    /**
     * 🔥 커뮤니티 통합 필터링 조회 (동적 쿼리)
     * - category, region, keyword, searchType 기반 필터링
     * - keyword는 부분 검색 (LIKE %keyword%)
     */
    @Query("SELECT c FROM Community c WHERE " +
            "(:category IS NULL OR c.category = :category) AND " +
            "(:region IS NULL OR c.region = :region) AND " +
            "(" +
            "  :keyword IS NULL OR " +
            "  (:searchType = 'title' AND c.title LIKE CONCAT('%', :keyword, '%')) OR " +
            "  (:searchType = 'author' AND c.authorNickname LIKE CONCAT('%', :keyword, '%'))" +
            ")")
    Page<Community> findWithFilters(
            @Param("category") String category,
            @Param("region") String region,
            @Param("searchType") String searchType,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // =========================
    // 기본 조회
    // =========================

    Page<Community> findByCategory(String category, Pageable pageable);

    Page<Community> findByRegion(String region, Pageable pageable);

    Page<Community> findByTitleContaining(String keyword, Pageable pageable);

    Page<Community> findByAuthorNicknameContaining(String keyword, Pageable pageable);

    // =========================
    // 정렬 기준
    // =========================

    // 조회수 기준
    Page<Community> findByOrderByViewCountDesc(Pageable pageable);

    // 🔥 공유수 기준 (recommendCount 제거 → shareCount로 대체)
    Page<Community> findByOrderByShareCountDesc(Pageable pageable);

    // 🔥 좋아요수 기준
    Page<Community> findByOrderByLikeCountDesc(Pageable pageable);

    // =========================
    // 🔥 인기글 (커스텀 정렬)
    // =========================
    // 좋아요 + 조회수 + 공유수 가중치 반영

    @Query("SELECT c FROM Community c ORDER BY (c.likeCount * 3 + c.shareCount * 5 + c.viewCount) DESC")
    Page<Community> findPopularPosts(Pageable pageable);
}