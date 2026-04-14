package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.CommunityRequest;
import com.fiveguys.trip_planner.entity.CommunityImage;
import com.fiveguys.trip_planner.response.CommunityResponse;
import com.fiveguys.trip_planner.service.CommunityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "💬 커뮤니티 API", description = "여행 정보 공유 및 게시글 관리")
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    // =========================
    // 🔹 게시글 생성
    // =========================
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(
            @RequestBody CommunityRequest request
    ) {
        try {
            // 🔥 authorId, authorNickname은 Service에서 처리 (인증 기반)
            Long postId = communityService.createPost(request);

            return ResponseEntity.status(201).body(
                    Map.of("success", true, "postId", postId)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "서버 오류"));
        }
    }

    // =========================
    // 🔹 게시글 목록 조회
    // =========================
    @GetMapping("/posts")
    public ResponseEntity<Page<CommunityResponse>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(
                communityService.getPosts(page, size, category, region, searchType, keyword)
        );
    }

    // =========================
    // 🔹 게시글 단건 조회
    // =========================
    @GetMapping("/posts/{postId}")
    public ResponseEntity<CommunityResponse> getPost(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(communityService.getPost(postId));
    }

    // =========================
    // 🔹 조회수 증가
    // =========================
    @PatchMapping("/posts/{postId}/view")
    public ResponseEntity<?> viewPost(@PathVariable Long postId) {
        communityService.viewPost(postId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // =========================
    // 🔹 공유 증가
    // =========================
    @PatchMapping("/posts/{postId}/share")
    public ResponseEntity<?> sharePost(@PathVariable Long postId) {
        communityService.incrementShare(postId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // =========================
    // 🔥 좋아요 토글
    // =========================
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long postId
    ) {

        boolean liked = communityService.toggleLike(postId);
        Long likeCount = communityService.getLikeCount(postId);

        return ResponseEntity.ok(
                Map.of(
                        "liked", liked,
                        "likeCount", likeCount
                )
        );
    }

    // =========================
    // 🔥 좋아요 상태 조회
    // =========================
    @GetMapping("/posts/{postId}/like-status")
    public ResponseEntity<?> getLikeStatus(
            @PathVariable Long postId
    ) {
        boolean liked = communityService.isLiked(postId);
        Long likeCount = communityService.getLikeCount(postId);

        return ResponseEntity.ok(
                Map.of(
                        "liked", liked,
                        "likeCount", likeCount
                )
        );
    }

    // =========================
    // 🔹 이미지 업로드
    // =========================
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        Long imageId = communityService.uploadImage(file);
        return ResponseEntity.ok(Map.of("imageId", imageId));
    }

    // =========================
    // 🔹 이미지 조회
    // =========================
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {

        CommunityImage image = communityService.getImageEntity(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + image.getOriginalName() + "\"")
                .body(image.getData());
    }

    // =========================
    // 🔹 게시글 수정을 위한 데이터 조회
    // =========================
    @GetMapping("/posts/{postId}/edit")
    public ResponseEntity<CommunityResponse> getPostForEdit(
            @PathVariable Long postId
    ) {
        // 🔥 조회수 증가 없는 조회용 메서드 쓰는 게 베스트
        CommunityResponse response = communityService.getPost(postId);
        return ResponseEntity.ok(response);
    }

    // =========================
    // 🔹 게시글 수정 실행
    // =========================
    @PutMapping("/posts/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @RequestBody CommunityRequest request
    ) {
        try {
            communityService.updatePost(postId, request);
            return ResponseEntity.ok(
                    Map.of("success", true, "message", "게시글이 수정되었습니다.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "서버 오류"));
        }
    }

    // =========================
    // 🔹 게시글 삭제
    // =========================
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        try {
            communityService.deletePost(postId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "삭제 실패"));
        }
    }
}