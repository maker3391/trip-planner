package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    /**
     * 🔥 커뮤니티 통합 필터링 조회 (N+1 방지 + pageable 안정화)
     */
    @Query(value = "SELECT c FROM Community c LEFT JOIN FETCH c.author a WHERE " +
            "(:category IS NULL OR c.category = :category) AND " +
            "(:region IS NULL OR c.region = :region) AND " +
            "(" +
            "  :keyword IS NULL OR " +
            "  (:searchType = 'title' AND c.title LIKE CONCAT('%', :keyword, '%')) OR " +
            "  (:searchType = 'author' AND a.nickname LIKE CONCAT('%', :keyword, '%'))" +
            ")",
            countQuery = "SELECT COUNT(c) FROM Community c LEFT JOIN c.author a WHERE " +
                    "(:category IS NULL OR c.category = :category) AND " +
                    "(:region IS NULL OR c.region = :region) AND " +
                    "(" +
                    "  :keyword IS NULL OR " +
                    "  (:searchType = 'title' AND c.title LIKE CONCAT('%', :keyword, '%')) OR " +
                    "  (:searchType = 'author' AND a.nickname LIKE CONCAT('%', :keyword, '%'))" +
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

    /**
     * 🔥 작성자 닉네임 검색 (N+1 방지)
     */
    @Query(value = "SELECT c FROM Community c JOIN FETCH c.author a WHERE a.nickname LIKE CONCAT('%', :keyword, '%')",
            countQuery = "SELECT COUNT(c) FROM Community c JOIN c.author a WHERE a.nickname LIKE CONCAT('%', :keyword, '%')")
    Page<Community> findByAuthorNicknameContaining(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // =========================
    // 정렬 기준
    // =========================

    Page<Community> findByOrderByViewCountDesc(Pageable pageable);

    Page<Community> findByOrderByShareCountDesc(Pageable pageable);

    Page<Community> findByOrderByLikeCountDesc(Pageable pageable);

    // =========================
    // 🔥 인기글
    // =========================

    @Query("SELECT c FROM Community c ORDER BY (c.likeCount * 3 + c.shareCount * 5 + c.viewCount) DESC")
    Page<Community> findPopularPosts(Pageable pageable);

    @Modifying
    @Query("UPDATE Community c SET c.viewCount = c.viewCount + 1 WHERE c.id = :postId")
    void updateViewCount(@Param("postId") Long postId);
}