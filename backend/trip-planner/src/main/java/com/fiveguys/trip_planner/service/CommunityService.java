package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.CommunityRequest;
import com.fiveguys.trip_planner.response.CommunityResponse;
import com.fiveguys.trip_planner.entity.Community;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.repository.CommunityRepository;
import com.fiveguys.trip_planner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    // 🔥 게시글 작성
    @Transactional
    public Long createPost(CommunityRequest request) {

        validateRequest(request);

        // XSS 방어
        String safeContent = Jsoup.clean(request.getContent(), Safelist.relaxed());

        // 작성자 닉네임 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 작성자 ID입니다."));
        String authorNickname = user.getNickname();

        // 엔티티 생성
        Community community = Community.builder()
                .category(request.getCategory())
                .region(request.getRegion())
                .title(request.getTitle().trim())
                .content(safeContent)
                .departure(request.getDeparture())
                .arrival(request.getArrival())
                .tags(request.getTags())
                .rating(request.getRating())
                .authorNickname(authorNickname)
                .viewCount(0L)
                .recommendCount(0L)
                .build();

        return communityRepository.save(community).getId();
    }

    // 🔥 게시글 목록 조회 (DTO 변환)
    public Page<CommunityResponse> getPosts(int page, int size) {
        return communityRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        ).map(CommunityResponse::from);
    }

    public void viewPost(Long postId) {
        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        community.incrementViewCount();
    }

    // 🔥 게시글 좋아요 증가
    @Transactional
    public void recommendPost(Long postId) {
        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        community.incrementRecommend();
    }

    // 🔥 게시글 좋아요 감소 (선택)
    @Transactional
    public void unRecommendPost(Long postId) {
        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        community.decrementRecommend();
    }

    // 🔥 검증 로직
    private void validateRequest(CommunityRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("제목은 필수입니다.");
        if (request.getContent() == null || request.getContent().trim().equals("<p><br></p>"))
            throw new IllegalArgumentException("내용을 입력해주세요.");
        if (isPlanCategory(request.getCategory())) {
            if (isEmpty(request.getDeparture()) || isEmpty(request.getArrival()))
                throw new IllegalArgumentException("출발지와 도착지는 필수입니다.");
        }
        if (isRatingCategory(request.getCategory())) {
            if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5)
                throw new IllegalArgumentException("평점은 1~5 사이여야 합니다.");
        }
        if (request.getUserId() == null)
            throw new IllegalArgumentException("작성자 ID는 필수입니다.");
    }

    private boolean isPlanCategory(String category) {
        return category != null && (category.equals("여행플랜 공유") || category.equals("당일치기 친구 찾기"));
    }

    private boolean isRatingCategory(String category) {
        return category != null && (category.equals("맛집게시판")
                || category.equals("사진게시판")
                || category.equals("후기게시판"));
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}