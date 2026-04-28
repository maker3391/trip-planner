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
    // 🔥 통합 필터 + 검색 (수정 완료)
    // =========================
    @Query(
            value =
                    "SELECT c FROM Community c LEFT JOIN c.author a WHERE " +

                            // =========================
                            // 1️⃣ 카테고리 필터
                            // =========================
                            "(:category IS NULL OR c.category IN :category) AND " +

                            // =========================
                            // 2️⃣ 지역 필터
                            // =========================
                            "(" +
                            "   (:region IS NULL) OR " +
                            "   (c.category IN ('자유게시판', '공지게시판')) OR " +
                            "   (c.region IN :region)" +
                            ") AND " +

                            // =========================
                            // 3️⃣ 검색 필터 (🔥 핵심 수정)
                            // =========================
                            "(" +
                            "   :keyword IS NULL OR " +

                            // title
                            "   (:searchType = 'title' AND c.title LIKE CONCAT('%', :keyword, '%')) OR " +

                            // author
                            "   (:searchType = 'author' AND a.nickname LIKE CONCAT('%', :keyword, '%')) OR " +

                            // content
                            "   (:searchType = 'content' AND c.content LIKE CONCAT('%', :keyword, '%')) OR " +

                            // tag
                            "   (:searchType = 'tag' AND c.tags LIKE CONCAT('%', :keyword, '%')) OR " +

                            // title + author
                            "   (:searchType = 'title_author' AND " +
                            "       (c.title LIKE CONCAT('%', :keyword, '%') OR a.nickname LIKE CONCAT('%', :keyword, '%'))" +
                            "   ) OR " +

                            // title + content
                            "   (:searchType = 'title_content' AND " +
                            "       (c.title LIKE CONCAT('%', :keyword, '%') OR c.content LIKE CONCAT('%', :keyword, '%'))" +
                            "   ) OR " +

                            // 🔥 fallback (searchType 없을 때)
                            "   (:searchType IS NULL AND " +
                            "       (c.title LIKE CONCAT('%', :keyword, '%') OR c.content LIKE CONCAT('%', :keyword, '%'))" +
                            "   )" +
                            ")",

            countQuery =
                    "SELECT COUNT(c) FROM Community c LEFT JOIN c.author a WHERE " +

                            "(:category IS NULL OR c.category IN :category) AND " +

                            "(" +
                            "   (:region IS NULL) OR " +
                            "   (c.category IN ('자유게시판', '공지게시판')) OR " +
                            "   (c.region IN :region)" +
                            ") AND " +

                            "(" +
                            "   :keyword IS NULL OR " +

                            "   (:searchType = 'title' AND c.title LIKE CONCAT('%', :keyword, '%')) OR " +
                            "   (:searchType = 'author' AND a.nickname LIKE CONCAT('%', :keyword, '%')) OR " +
                            "   (:searchType = 'content' AND c.content LIKE CONCAT('%', :keyword, '%')) OR " +
                            "   (:searchType = 'tag' AND c.tags LIKE CONCAT('%', :keyword, '%')) OR " +

                            "   (:searchType = 'title_author' AND " +
                            "       (c.title LIKE CONCAT('%', :keyword, '%') OR a.nickname LIKE CONCAT('%', :keyword, '%'))" +
                            "   ) OR " +

                            "   (:searchType = 'title_content' AND " +
                            "       (c.title LIKE CONCAT('%', :keyword, '%') OR c.content LIKE CONCAT('%', :keyword, '%'))" +
                            "   ) OR " +

                            "   (:searchType IS NULL AND " +
                            "       (c.title LIKE CONCAT('%', :keyword, '%') OR c.content LIKE CONCAT('%', :keyword, '%'))" +
                            "   )" +
                            ")"
    )
    Page<Community> findWithFilters(
            @Param("category") List<String> category,
            @Param("region") List<String> region,
            @Param("searchType") String searchType,
            @Param("keyword") String keyword,
            Pageable pageable
    );


    // =========================
    // 🔹 기본 검색
    // =========================
    Page<Community> findByTitleContaining(String keyword, Pageable pageable);


    // =========================
    // 🔥 작성자 검색
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
    // 🔹 정렬
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
    // 🔹 조회수 증가
    // =========================
    @Modifying
    @Query("UPDATE Community c SET c.viewCount = c.viewCount + 1 WHERE c.id = :postId")
    void updateViewCount(@Param("postId") Long postId);


    // =========================
    // 🔹 여행 계획 연결 조회
    // =========================
    Optional<Community> findFirstByTripPlan(TripPlan tripPlan);


    // =========================
    // 🔥 마이페이지
    // =========================
    Page<Community> findByAuthor(com.fiveguys.trip_planner.entity.User author, Pageable pageable);
}