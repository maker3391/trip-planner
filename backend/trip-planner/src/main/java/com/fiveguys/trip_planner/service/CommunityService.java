package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.CommunityRequest;
import com.fiveguys.trip_planner.entity.Community;
import com.fiveguys.trip_planner.entity.CommunityImage;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.repository.CommunityImageRepository;
import com.fiveguys.trip_planner.repository.CommunityRepository;
import com.fiveguys.trip_planner.repository.UserRepository;
import com.fiveguys.trip_planner.response.CommunityResponse;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final CommunityImageRepository communityImageRepository;

    /**
     * 🔥 게시글 작성
     */
    @Transactional
    public Long createPost(CommunityRequest request) {
        validateRequest(request);

        // XSS 방어
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
                .recommendCount(0L)
                .build();

        Community savedCommunity = communityRepository.save(community);

        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            for (Long imageId : request.getImageIds()) {
                communityImageRepository.findById(imageId).ifPresent(image -> {
                    image.setCommunity(savedCommunity);
                });
            }
        }

        return savedCommunity.getId();
    }

    /**
     * 🔥 게시글 목록 조회 (통합 필터링 및 페이징)
     * 카테고리, 지역, 검색어에 따른 동적 쿼리를 수행합니다.
     */
    public Page<CommunityResponse> getPosts(int page, int size, String category, String region, String searchType, String keyword) {
        // 1. 페이지네이션 설정 (ID 내림차순 - 최신순)
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        // 2. 검색어 전처리 (프론트에서 '전체보기'를 선택하면 null로 처리)
        String filterCategory = ("전체보기".equals(category)) ? null : category;
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? "%" + keyword.trim() + "%" : null;

        // 3. 필터링된 결과 조회
        return communityRepository.findWithFilters(
                filterCategory,
                region,
                searchType,
                searchKeyword,
                pageable
        ).map(CommunityResponse::from);
    }

    /**
     * 🔥 게시글 단건 조회
     */
    public CommunityResponse getPost(Long id) {
        Community post = communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        List<Long> imageIds = communityImageRepository.findByCommunityId(id)
                .stream()
                .map(CommunityImage::getId)
                .collect(Collectors.toList());

        return CommunityResponse.builder()
                .id(post.getId())
                .category(post.getCategory())
                .region(post.getRegion())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(post.getAuthorNickname())
                .tags(post.getTags())
                .viewCount(post.getViewCount())
                .recommendCount(post.getRecommendCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .departure(post.getDeparture())
                .arrival(post.getArrival())
                .rating(post.getRating())
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

    @Transactional
    public void viewPost(Long postId) {
        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        community.incrementViewCount();
    }

    @Transactional
    public void recommendPost(Long postId) {
        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        community.incrementRecommend();
    }

    @Transactional
    public void unRecommendPost(Long postId) {
        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        community.decrementRecommend();
    }

    // --- 내부 검증 로직 ---

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
        return category != null && (category.equals("맛집게시판") || category.equals("사진게시판") || category.equals("후기게시판"));
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}