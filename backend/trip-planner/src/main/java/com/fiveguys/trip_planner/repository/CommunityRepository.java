package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.Community;
import com.fiveguys.trip_planner.entity.TripPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    // =========================
    // 🔥 공지 상단 고정용 (최신 2개)
    // =========================
    @Query("SELECT c FROM Community c WHERE c.category = '공지게시판' ORDER BY c.id DESC")
    List<Community> findTop2Notices(Pageable pageable);


    // =========================
    // 🔥 통합 필터 + 검색 (안정화 버전)
    // =========================
    @Query(
            value = "SELECT c FROM Community c LEFT JOIN c.author a WHERE " +
                    "(:category IS NULL OR c.category = :category) AND " +
                    "(:region IS NULL OR c.region = :region) AND " +
                    "(" +
                    "  :keyword IS NULL OR :searchType IS NULL OR " +

                    // 제목
                    "  (:searchType = 'title' AND c.title LIKE CONCAT('%', :keyword, '%')) OR " +

                    // 작성자
                    "  (:searchType = 'author' AND a.nickname LIKE CONCAT('%', :keyword, '%')) OR " +

                    // 내용
                    "  (:searchType = 'content' AND c.content LIKE CONCAT('%', :keyword, '%')) OR " +

                    // 태그
                    "  (:searchType = 'tag' AND c.tags LIKE CONCAT('%', :keyword, '%')) OR " +

                    // 제목 + 작성자
                    "  (:searchType = 'title_author' AND " +
                    "     (c.title LIKE CONCAT('%', :keyword, '%') OR a.nickname LIKE CONCAT('%', :keyword, '%'))" +
                    "  ) OR " +

                    // 제목 + 내용
                    "  (:searchType = 'title_content' AND " +
                    "     (c.title LIKE CONCAT('%', :keyword, '%') OR c.content LIKE CONCAT('%', :keyword, '%'))" +
                    "  )" +

                    ")",

            countQuery = "SELECT COUNT(c) FROM Community c LEFT JOIN c.author a WHERE " +
                    "(:category IS NULL OR c.category = :category) AND " +
                    "(:region IS NULL OR c.region = :region) AND " +
                    "(" +
                    "  :keyword IS NULL OR :searchType IS NULL OR " +

                    "  (:searchType = 'title' AND c.title LIKE CONCAT('%', :keyword, '%')) OR " +
                    "  (:searchType = 'author' AND a.nickname LIKE CONCAT('%', :keyword, '%')) OR " +
                    "  (:searchType = 'content' AND c.content LIKE CONCAT('%', :keyword, '%')) OR " +
                    "  (:searchType = 'tag' AND c.tags LIKE CONCAT('%', :keyword, '%')) OR " +

                    "  (:searchType = 'title_author' AND " +
                    "     (c.title LIKE CONCAT('%', :keyword, '%') OR a.nickname LIKE CONCAT('%', :keyword, '%'))" +
                    "  ) OR " +

                    "  (:searchType = 'title_content' AND " +
                    "     (c.title LIKE CONCAT('%', :keyword, '%') OR c.content LIKE CONCAT('%', :keyword, '%'))" +
                    "  )" +

                    ")"
    )
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


    // =========================
    // 🔥 작성자 검색 (N+1 방지)
    // =========================
    @Query(
            value = "SELECT c FROM Community c JOIN c.author a WHERE a.nickname LIKE CONCAT('%', :keyword, '%')",
            countQuery = "SELECT COUNT(c) FROM Community c JOIN c.author a WHERE a.nickname LIKE CONCAT('%', :keyword, '%')"
    )
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


    // =========================
    // 조회수 증가
    // =========================
    @Modifying
    @Query("UPDATE Community c SET c.viewCount = c.viewCount + 1 WHERE c.id = :postId")
    void updateViewCount(@Param("postId") Long postId);

    Optional<Community> findFirstByTripPlan(TripPlan tripPlan);

    // =========================
    // 🔥 마이페이지 전용: 내가 쓴 글 조회
    // =========================
        Page<Community> findByAuthor(com.fiveguys.trip_planner.entity.User author, Pageable pageable);
}