package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.CommunityRequest;
import com.fiveguys.trip_planner.entity.*;
import com.fiveguys.trip_planner.repository.*;
import com.fiveguys.trip_planner.response.CommunityResponse;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final CommunityImageRepository communityImageRepository;
    private final CommunityLikeRepository communityLikeRepository;

    /**
     * 🔥 게시글 작성
     */
    @Transactional
    public Long createPost(CommunityRequest request) {
        validateRequest(request);

        String safeContent = Jsoup.clean(request.getContent(), Safelist.relaxed());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 작성자 ID입니다."));

        Community community = Community.builder()
                .category(request.getCategory())
                .region(request.getRegion())
                .title(request.getTitle().trim())
                .content(safeContent)
                .departure(request.getDeparture())
                .arrival(request.getArrival())
                .tags(request.getTags())
                .rating(request.getRating())
                .authorNickname(user.getNickname())
                .viewCount(0L)
                .shareCount(0L)
                .likeCount(0L)
                .build();

        Community saved = communityRepository.save(community);

        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            for (Long imageId : request.getImageIds()) {
                communityImageRepository.findById(imageId)
                        .ifPresent(img -> img.setCommunity(saved));
            }
        }

        return saved.getId();
    }

    /**
     * 🔥 게시글 목록 조회
     */
    public Page<CommunityResponse> getPosts(int page, int size,
                                            String category, String region,
                                            String searchType, String keyword) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        String filterCategory = ("전체보기".equals(category)) ? null : category;

        if (!"title".equals(searchType) && !"author".equals(searchType)) {
            searchType = null;
        }

        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty())
                ? keyword.trim()
                : null;

        return communityRepository.findWithFilters(
                filterCategory,
                region,
                searchType,
                cleanKeyword,
                pageable
        ).map(CommunityResponse::from);
    }

    /**
     * 🔥 게시글 단건 조회 (likedByMe 포함)
     */
    public CommunityResponse getPost(Long id) {

        Community post = communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        List<Long> imageIds = communityImageRepository.findByCommunityId(id)
                .stream()
                .map(CommunityImage::getId)
                .toList();

        boolean likedByMe = false;

        try {
            User user = getCurrentUser();
            likedByMe = communityLikeRepository.existsByUserAndCommunity(user, post);
        } catch (Exception ignored) {}

        return CommunityResponse.builder()
                .id(post.getId())
                .category(post.getCategory())
                .region(post.getRegion())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(post.getAuthorNickname())
                .tags(post.getTags())
                .viewCount(post.getViewCount())
                .shareCount(post.getShareCount())
                .likeCount(post.getLikeCount())
                .likedByMe(likedByMe)
                .rating(post.getRating())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .departure(post.getDeparture())
                .arrival(post.getArrival())
                .imageIds(imageIds)
                .build();
    }

    /**
     * 🔥 이미지 업로드
     */
    @Transactional
    public Long uploadImage(MultipartFile file) {
        try {
            CommunityImage image = CommunityImage.builder()
                    .originalName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .data(file.getBytes())
                    .build();

            return communityImageRepository.save(image).getId();

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 중 오류 발생", e);
        }
    }

    public CommunityImage getImageEntity(Long id) {
        return communityImageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다. ID: " + id));
    }

    /**
     * 🔥 조회수 증가
     */
    @Transactional
    public void viewPost(Long postId) {
        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        community.incrementViewCount();
    }

    /**
     * 🔥 공유 증가
     */
    @Transactional
    public void incrementShare(Long postId) {
        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        community.incrementShareCount();
    }

    /**
     * 🔥 좋아요 토글 (핵심)
     */
    @Transactional
    public boolean toggleLike(Long communityId) {

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        User user = getCurrentUser();

        return communityLikeRepository.findByUserAndCommunity(user, community)
                .map(existing -> {
                    communityLikeRepository.delete(existing);
                    community.decrementLikeCount();
                    return false;
                })
                .orElseGet(() -> {
                    CommunityLike like = CommunityLike.of(user, community);
                    communityLikeRepository.save(like);
                    community.incrementLikeCount();
                    return true;
                });
    }

    /**
     * 🔥 좋아요 개수 조회
     */
    public Long getLikeCount(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"))
                .getLikeCount();
    }

    /**
     * 🔥 내가 눌렀는지
     */
    public boolean isLiked(Long communityId) {

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        User user = getCurrentUser();

        return communityLikeRepository.existsByUserAndCommunity(user, community);
    }

    /**
     * 🔥 로그인 유저 가져오기 (공통)
     */
    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // =========================
    // 내부 검증
    // =========================

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
        return category != null &&
                (category.equals("여행플랜 공유") || category.equals("당일치기 친구 찾기"));
    }

    private boolean isRatingCategory(String category) {
        return category != null &&
                (category.equals("맛집게시판") || category.equals("사진게시판") || category.equals("후기게시판"));
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}