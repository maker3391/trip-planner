package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.Community;
import com.fiveguys.trip_planner.entity.TripPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// =====================================================================
// [요구사항 확인 및 반영 사항]
// 규칙 1 (카테고리 우선도 - 1차 필터링 후 2차 종속 판독):
// 기존의 단순 AND/OR 나열을 수정하여,
// 1) 입력된 카테고리를 먼저 판별하도록 기준을 잡고,
// 2) 지역 필터는 '해당 게시글의 카테고리가 지역 정보와 무관한 경우(자유/공지)'에는 완전히 무시되도록 논리식을 그룹화했습니다.
// 이로써 카테고리와 맞지 않는 지역이 선택되어 아무것도 나오지 않는 휴먼 에러를 방지합니다.
// =====================================================================

public interface CommunityRepository extends JpaRepository<Community, Long> {

    // =========================
    // 🔥 공지 상단 고정용 (최신 2개)
    // =========================
    @Query("SELECT c FROM Community c WHERE c.category = '공지게시판' ORDER BY c.id DESC")
    List<Community> findTop2Notices(Pageable pageable);


    // =========================
    // 🔥 통합 필터 + 검색 (핵심)
    // =========================
    @Query(
            value = "SELECT c FROM Community c LEFT JOIN c.author a WHERE " +
                    // 1차 필터: 카테고리 우선 거르기 (다중선택 OR 연산 포함)
                    "(:category IS NULL OR c.category IN :category) AND " +

                    // 2차 필터: 지역 판독 (카테고리에 종속)
                    "(" +
                    "   (:region IS NULL) OR " + // 지역 필터가 '전체'일 때
                    "   (c.category IN ('자유게시판', '공지게시판')) OR " + // 지역 속성이 없는 카테고리는 지역 필터 무시
                    "   (c.region IN :region)" + // 그 외(여행플랜, 후기 등)는 다중 지역 필터 적용
                    ") AND " +

                    // 검색 필터
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

                    ")",

            countQuery = "SELECT COUNT(c) FROM Community c LEFT JOIN c.author a WHERE " +
                    "(:category IS NULL OR c.category IN :category) AND " +
                    "(" +
                    "   (:region IS NULL) OR " +
                    "   (c.category IN ('자유게시판', '공지게시판')) OR " +
                    "   (c.region IN :region)" +
                    ") AND " +
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
    // 🔹 정렬 기준
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
    // 🔥 마이페이지 전용: 내가 쓴 글 조회
    // =========================
        Page<Community> findByAuthor(com.fiveguys.trip_planner.entity.User author, Pageable pageable);
}