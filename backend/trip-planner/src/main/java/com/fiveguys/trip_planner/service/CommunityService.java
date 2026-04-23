package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.CommunityCommentRequest;
import com.fiveguys.trip_planner.dto.CommunityRequest;
import com.fiveguys.trip_planner.entity.*;
import com.fiveguys.trip_planner.repository.*;
import com.fiveguys.trip_planner.response.CommunityCommentResponse;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    // =========================
    // 🔥 이미지 저장 방식 선택
    // "DB" or "S3"
    // =========================
    private final String IMAGE_STORAGE = "DB";

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final CommunityImageRepository communityImageRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final TripPlanRepository tripPlanRepository;

    // =========================
    // 🔹 게시글 생성
    // =========================
    @Transactional
    public Long createPost(CommunityRequest request) {

        validateRequest(request);

        String safeContent = Jsoup.clean(request.getContent(), Safelist.relaxed());
        User user = getCurrentUser();

        TripPlan tripPlan = null;
        if(request.getTripPlanId() != null) {
            tripPlan = tripPlanRepository.findById(request.getTripPlanId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 여행 계획 입니다."));
        }

        Community community = Community.builder()
                .category(request.getCategory())
                .region(request.getRegion())
                .title(request.getTitle().trim())
                .content(safeContent)
                .departure(request.getDeparture())
                .arrival(request.getArrival())
                .tags(request.getTags())
                .rating(request.getRating())
                .author(user)
                .tripPlan(tripPlan)
                .viewCount(0L)
                .shareCount(0L)
                .likeCount(0L)
                .build();

        Community saved = communityRepository.save(community);

        if (request.getImageIds() != null) {
            request.getImageIds().forEach(imageId ->
                    communityImageRepository.findById(imageId)
                            .ifPresent(img -> img.setCommunity(saved))
            );
        }

        return saved.getId();
    }

    // =========================
    // 🔹 게시글 목록 + 검색 + 필터
    // =========================
    public Page<CommunityResponse> getPosts(
            int page,
            int size,
            String category,
            String region,
            String searchType,
            String keyword
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        String filterCategory = ("전체보기".equals(category)) ? null : category;
        String filterRegion = ("전체".equals(region)) ? null : region;

        String cleanKeyword = (keyword == null || keyword.trim().isEmpty())
                ? null
                : keyword.trim();

        String validSearchType =
                (
                        "title".equals(searchType) ||
                                "author".equals(searchType) ||
                                "content".equals(searchType) ||
                                "tag".equals(searchType) ||
                                "title_author".equals(searchType) ||
                                "title_content".equals(searchType)
                )
                        ? searchType
                        : null;

        if (cleanKeyword == null) {
            validSearchType = null;
        }

        User currentUser = null;
        try {
            currentUser = getCurrentUser();
        } catch (Exception ignored) {}

        User finalUser = currentUser;

        return communityRepository.findWithFilters(
                filterCategory,
                filterRegion,
                validSearchType,
                cleanKeyword,
                pageable
        ).map(post -> {

            boolean likedByMe = false;

            if (finalUser != null) {
                likedByMe = communityLikeRepository.existsByUserAndCommunity(finalUser, post);
            }

            return CommunityResponse.from(post, likedByMe);
        });
    }

    // =========================
    // 🔹 게시글 상세 조회
    // =========================
    public CommunityResponse getPost(Long id) {

        Community post = communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        boolean likedByMe = false;

        try {
            User user = getCurrentUser();
            likedByMe = communityLikeRepository.existsByUserAndCommunity(user, post);
        } catch (Exception ignored) {}

        return CommunityResponse.from(post, likedByMe);
    }

    // =========================
    // 🔹 게시글 수정
    // =========================
    @Transactional
    public void updatePost(Long postId, CommunityRequest request) {

        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        User user = getCurrentUser();

        if (!community.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인이 작성한 글만 수정할 수 있습니다.");
        }

        TripPlan tripPlan = null;
        if (request.getTripPlanId() != null) {
            tripPlan = tripPlanRepository.findById(request.getTripPlanId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 여행 계획입니다."));
        }

        validateRequest(request);

        String safeContent = Jsoup.clean(request.getContent(), Safelist.relaxed());

        community.update(
                request.getCategory(),
                request.getRegion(),
                request.getTitle().trim(),
                safeContent,
                request.getDeparture(),
                request.getArrival(),
                request.getTags(),
                request.getRating(),
                tripPlan
        );

        if (request.getImageIds() != null) {
            request.getImageIds().forEach(imageId ->
                    communityImageRepository.findById(imageId)
                            .ifPresent(img -> img.setCommunity(community))
            );
        }
    }

    // =========================
    // 🔹 게시글 삭제
    // =========================
    @Transactional
    public void deletePost(Long postId) {

        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        User user = getCurrentUser();

        boolean isAuthor = community.getAuthor().getId().equals(user.getId());
        boolean isAdmin = "ADMIN".equals(user.getRole());

        if (!isAuthor && !isAdmin) {
            throw new IllegalArgumentException("본인이 작성한 글만 삭제할 수 있습니다.");
        }

        communityRepository.delete(community);
    }

    // =========================
    // 🔹 이미지 업로드
    // =========================
    @Transactional
    public Long uploadImage(MultipartFile file) {

        if ("S3".equalsIgnoreCase(IMAGE_STORAGE)) {
            return uploadToS3(file);
        }

        return uploadToDB(file);
    }

    private Long uploadToDB(MultipartFile file) {
        try {
            CommunityImage image = CommunityImage.builder()
                    .originalName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .data(file.getBytes())
                    .build();

            return communityImageRepository.save(image).getId();

        } catch (IOException e) {
            throw new RuntimeException("DB 이미지 저장 실패", e);
        }
    }

    private Long uploadToS3(MultipartFile file) {
        try {
            // 🔥 나중에 진짜 S3 업로드 로직 넣는 자리
            String s3Url = "https://s3.amazonaws.com/bucket/" + file.getOriginalFilename();

            CommunityImage image = CommunityImage.builder()
                    .originalName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .data(s3Url.getBytes()) // 임시 (⚠️ 추후 imageUrl 필드 추천)
                    .build();

            return communityImageRepository.save(image).getId();

        } catch (Exception e) {
            throw new RuntimeException("S3 이미지 저장 실패", e);
        }
    }

    // =========================
    // 🔹 이미지 조회
    // =========================
    public byte[] getImage(Long id) {

        if ("S3".equalsIgnoreCase(IMAGE_STORAGE)) {
            return getFromS3(id);
        }

        return getFromDB(id);
    }

    private byte[] getFromDB(Long id) {
        return communityImageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지 없음"))
                .getData();
    }

    private byte[] getFromS3(Long id) {
        // 🔥 나중에 S3 다운로드
        return new byte[0];
    }

    public String getImageContentType(Long id) {
        return communityImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("이미지 없음"))
                .getContentType();
    }

    public String getImageName(Long id) {
        return communityImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("이미지 없음"))
                .getOriginalName();
    }

    // =========================
    // 🔹 조회수 증가
    // =========================
    @Transactional
    public void viewPost(Long postId) {

        if (!communityRepository.existsById(postId)) {
            throw new IllegalArgumentException("게시글 없음");
        }

        communityRepository.updateViewCount(postId);
    }

    // =========================
    // 🔹 공유 증가
    // =========================
    @Transactional
    public void incrementShare(Long postId) {

        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        community.incrementShareCount();
    }

    // =========================
    // 🔥 좋아요 토글
    // =========================
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

    // =========================
    // 🔹 댓글 작성
    // =========================
    public void createComment(Long postId, Long userId, CommunityCommentRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        CommunityComment comment =
                CommunityComment.create(user, community, request.getComment());

        communityCommentRepository.save(comment);
        community.incrementCommentCount(); // ⭐ 중요
    }

    // =========================
    // 🔹 대댓글 작성
    // =========================
    public void createReply(Long postId, Long parentId, Long userId, CommunityCommentRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        CommunityComment parent = communityCommentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));

        CommunityComment reply =
                CommunityComment.createReply(user, community, request.getComment(), parent);

        communityCommentRepository.save(reply);
        community.incrementCommentCount();
    }

    // =========================
    // 🔹 댓글 조회
    // =========================
    @Transactional(readOnly = true)
    public CommunityCommentResponse getComments(Long postId, int page, int size) {

        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        Page<CommunityComment> parents =
                communityCommentRepository.findByCommunityAndParentIsNull(
                        community,
                        PageRequest.of(page, size, Sort.by("createdAt").descending())
                );

        List<CommunityComment> children =
                communityCommentRepository.findByParentIn(parents.getContent());

        Map<Long, List<CommunityComment>> childMap =
                children.stream()
                        .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        List<CommunityCommentResponse.CommentDto> result =
                parents.getContent().stream()
                        .map(parent -> new CommunityCommentResponse.CommentDto(
                                parent,
                                childMap.getOrDefault(parent.getId(), List.of())
                        ))
                        .toList();

        return new CommunityCommentResponse(result, parents.getTotalPages());
    }

    // =========================
    // 🔹 댓글 삭제
    // =========================
    public void deleteComment(Long commentId, Long userId) {

        CommunityComment comment = communityCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한 없음");
        }

        Community community = comment.getCommunity();

        communityCommentRepository.delete(comment);
        community.decrementCommentCount();
    }

    public Long getLikeCount(Long communityId) {

        return communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"))
                .getLikeCount();
    }

    public boolean isLiked(Long communityId) {

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        User user = getCurrentUser();

        return communityLikeRepository.existsByUserAndCommunity(user, community);
    }

    // =========================
    // 🔐 현재 유저
    // =========================
    private User getCurrentUser() {

        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        throw new RuntimeException("로그인 사용자 정보 없음");
    }

    // =========================
    // 🔥 검증 로직
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
    }

    private boolean isPlanCategory(String category) {
        return category != null &&
                (category.equals("여행플랜 공유") || category.equals("당일치기 친구 찾기"));
    }

    private boolean isRatingCategory(String category) {
        return category != null &&
                (category.equals("맛집게시판") ||
                        category.equals("사진게시판") ||
                        category.equals("후기게시판"));
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}