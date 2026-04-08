package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    // ✅ 카테고리별 조회 (페이징)
    Page<Community> findByCategory(String category, Pageable pageable);

    // ✅ 지역별 조회
    Page<Community> findByRegion(String region, Pageable pageable);

    // ✅ 카테고리 + 지역
    Page<Community> findByCategoryAndRegion(String category, String region, Pageable pageable);

    // ✅ 제목 검색
    Page<Community> findByTitleContaining(String keyword, Pageable pageable);

    // ✅ 제목 + 내용 검색 (추천)
    Page<Community> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);

    // ✅ 작성자 기준 검색
    Page<Community> findByAuthorNickname(String authorNickname, Pageable pageable);

    // ✅ 태그 기준 검색 (태그 문자열에 keyword 포함)
    Page<Community> findByTagsContaining(String keyword, Pageable pageable);

    // ✅ 평점 이상 검색
    Page<Community> findByRatingGreaterThanEqual(Integer rating, Pageable pageable);

    // ✅ 생성일 이후 검색
    Page<Community> findByCreatedAtAfter(LocalDateTime dateTime, Pageable pageable);

    // ✅ 조회수 기준 내림차순
    Page<Community> findByOrderByViewCountDesc(Pageable pageable);

    // ✅ 추천수 기준 내림차순
    Page<Community> findByOrderByRecommendCountDesc(Pageable pageable);
}