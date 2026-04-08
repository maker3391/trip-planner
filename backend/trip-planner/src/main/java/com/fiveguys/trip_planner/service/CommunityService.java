package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.CommunityRequest;
import com.fiveguys.trip_planner.entity.Community;
import com.fiveguys.trip_planner.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;

    @Transactional
    public Long createPost(CommunityRequest request) {

        // 🔥 1. 기본 검증
        validateRequest(request);

        // 🔥 2. XSS 방어 (HTML 정제)
        String safeContent = Jsoup.clean(
                request.getContent(),
                Safelist.relaxed()
        );

        // 🔥 3. 엔티티 생성
        Community community = Community.builder()
                .category(request.getCategory())
                .region(request.getRegion())
                .title(request.getTitle().trim())
                .content(safeContent)
                .departure(request.getDeparture())
                .arrival(request.getArrival())
                .tags(request.getTags())
                .rating(request.getRating())
                .build();

        return communityRepository.save(community).getId();
    }

    // 🔥 검증 로직 분리 (핵심)
    private void validateRequest(CommunityRequest request) {

        // 제목 검증
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }

        // 내용 검증 (Quill 대응)
        if (request.getContent() == null || request.getContent().trim().equals("<p><br></p>")) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }

        // ✈️ 여행 플랜 카테고리
        if (isPlanCategory(request.getCategory())) {
            if (isEmpty(request.getDeparture()) || isEmpty(request.getArrival())) {
                throw new IllegalArgumentException("출발지와 도착지는 필수입니다.");
            }
        }

        // ⭐ 평점 카테고리
        if (isRatingCategory(request.getCategory())) {
            if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
                throw new IllegalArgumentException("평점은 1~5 사이여야 합니다.");
            }
        }
    }

    private boolean isPlanCategory(String category) {
        if (category == null) return false; // 🔥 추가
        return category.equals("여행플랜 공유") || category.equals("당일치기 친구 찾기");
    }

    private boolean isRatingCategory(String category) {
        if (category == null) return false; // 🔥 추가
        return category.equals("맛집게시판")
                || category.equals("사진게시판")
                || category.equals("후기게시판");
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public Page<Community> getPosts(int page, int size) {
        // 최신 글이 먼저 나오도록 정렬
        return communityRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        );
    }
}