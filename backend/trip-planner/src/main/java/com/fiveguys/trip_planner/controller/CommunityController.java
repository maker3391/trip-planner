package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.CommunityRequest;
import com.fiveguys.trip_planner.entity.CommunityImage;
import com.fiveguys.trip_planner.response.CommunityResponse;
import com.fiveguys.trip_planner.service.CommunityService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "게시글 작성", description = "새로운 커뮤니티 게시글을 등록합니다. 작성자 정보는 토큰에서 추출합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
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
    @Operation(summary = "게시글 목록 조회", description = "페이징 및 필터링(카테고리, 지역, 검색어)을 적용하여 게시글 목록을 조회합니다.")
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
    // =========================
    // 🔹 게시글 단건 조회
    // =========================
    @Operation(summary = "게시글 상세 조회", description = "특정 ID의 게시글 상세 정보를 가져옵니다. 호출 시 조회수가 증가합니다.")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<CommunityResponse> getPost(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(communityService.getPost(postId));
    }

    // =========================
    // 🔹 조회수 증가
    // =========================
    @Operation(summary = "조회수 증가", description = "게시글 상세 페이지 진입 외에 수동으로 조회수를 1 증가시킬 때 사용합니다.")
    @PatchMapping("/posts/{postId}/view")
    public ResponseEntity<?> viewPost(@PathVariable Long postId) {
        communityService.viewPost(postId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // =========================
    // 🔹 공유 증가
    // =========================
    @Operation(summary = "공유 횟수 증가", description = "공유 버튼 클릭 시 공유 카운트를 1 증가시킵니다.")
    @PatchMapping("/posts/{postId}/share")
    public ResponseEntity<?> sharePost(@PathVariable Long postId) {
        communityService.incrementShare(postId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // =========================
    // 🔥 좋아요 토글
    // =========================
    @Operation(summary = "좋아요 토글", description = "게시글에 좋아요를 등록하거나 취소합니다.")
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
    @Operation(summary = "좋아요 상태 조회", description = "로그인한 유저가 현재 게시글에 좋아요를 눌렀는지 여부와 전체 좋아요 수를 확인합니다.")
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
    @Operation(summary = "이미지 업로드", description = "게시글에 포함될 이미지를 업로드하고 고유 ID를 반환받습니다.")
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        Long imageId = communityService.uploadImage(file);
        return ResponseEntity.ok(Map.of("imageId", imageId));
    }

    // =========================
    // 🔹 이미지 조회
    // =========================
    @Operation(summary = "이미지 조회", description = "이미지 ID를 통해 실제 이미지 데이터(파일)를 브라우저에 렌더링하거나 다운로드합니다.")
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
    @Operation(summary = "수정용 데이터 조회", description = "게시글 수정을 위해 기존 데이터를 조회합니다. (조회수가 증가하지 않습니다.)")
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
    @Operation(summary = "게시글 수정", description = "게시글의 제목, 내용, 카테고리 등을 수정합니다.")
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
    @Operation(summary = "게시글 삭제", description = "특정 게시글을 영구적으로 삭제합니다.")
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