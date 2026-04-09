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
     * 1. 게시글을 먼저 DB에 저장합니다.
     * 2. 요청(Request)에 포함된 이미지 ID 리스트를 순회하며 해당 이미지 엔티티에 게시글을 연결합니다.
     */
    @Transactional
    public Long createPost(CommunityRequest request) {
        validateRequest(request);

        // XSS 방어 (Quill 에디터의 HTML 태그 중 안전한 것만 허용)
        String safeContent = Jsoup.clean(request.getContent(), Safelist.relaxed());

        // 작성자 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 작성자 ID입니다."));

        // 1. 게시글 엔티티 생성 및 저장
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

        // 2. 업로드되었던 이미지들과 게시글 연결 (community_id 업데이트)
        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            for (Long imageId : request.getImageIds()) {
                communityImageRepository.findById(imageId).ifPresent(image -> {
                    // 연관 관계 편의 메서드 호출 (이미지 엔티티에 community_id 설정)
                    image.setCommunity(savedCommunity);
                });
            }
        }

        return savedCommunity.getId();
    }

    /**
     * 🔥 게시글 목록 조회 (페이징 처리)
     */
    public Page<CommunityResponse> getPosts(int page, int size) {
        return communityRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        ).map(CommunityResponse::from);
    }

    /**
     * 🔥 게시글 단건 조회
     */
    public CommunityResponse getPost(Long id) {
        Community post = communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 해당 게시글에 매핑된 이미지 ID 리스트 추출
        List<Long> imageIds = communityImageRepository.findByCommunityId(id)
                .stream()
                .map(CommunityImage::getId)
                .collect(Collectors.toList());

        // Response DTO 생성
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
     * 🔥 이미지 업로드 (바이너리 저장)
     * 게시글 작성 전, 사용자가 사진을 선택할 때마다 호출되어 DB에 미리 저장합니다.
     */
    @Transactional
    public Long uploadImage(MultipartFile file) {
        try {
            CommunityImage image = CommunityImage.builder()
                    .originalName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .data(file.getBytes()) // 원본 바이너리 데이터 추출
                    .build();
            return communityImageRepository.save(image).getId();
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 중 오류 발생", e);
        }
    }

    /**
     * 🔥 이미지 데이터 엔티티 조회
     * 컨트롤러에서 ResponseEntity<byte[]>를 만들 때 사용합니다.
     */
    public CommunityImage getImageEntity(Long id) {
        return communityImageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다. ID: " + id));
    }

    // --- 비즈니스 로직 (조회수/추천수) ---

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