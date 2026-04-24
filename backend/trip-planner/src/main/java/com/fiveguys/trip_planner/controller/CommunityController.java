package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.CommunityCommentRequest;
import com.fiveguys.trip_planner.dto.CommunityRequest;
import com.fiveguys.trip_planner.response.CommunityCommentResponse;
import com.fiveguys.trip_planner.response.CommunityResponse;
import com.fiveguys.trip_planner.service.CommunityService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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

    @Operation(summary = "게시글 작성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody CommunityRequest request) {
        Long postId = communityService.createPost(request);
        return ResponseEntity.status(201).body(Map.of("success", true, "postId", postId));
    }

    @Operation(summary = "게시글 목록 조회")
    @GetMapping("/posts")
    public ResponseEntity<Page<CommunityResponse>> getPosts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "region", required = false) String region,
            @RequestParam(name = "searchType", required = false) String searchType,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return ResponseEntity.ok(
                communityService.getPosts(page, size, category, region, searchType, keyword)
        );
    }

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<CommunityResponse> getPost(
            @PathVariable("postId") Long postId
    ) {
        return ResponseEntity.ok(communityService.getPost(postId));
    }

    @Operation(summary = "조회수 증가")
    @PatchMapping("/posts/{postId}/view")
    public ResponseEntity<?> viewPost(
            @PathVariable("postId") Long postId
    ) {
        communityService.viewPost(postId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @Operation(summary = "공유 횟수 증가")
    @PatchMapping("/posts/{postId}/share")
    public ResponseEntity<?> sharePost(
            @PathVariable("postId") Long postId
    ) {
        communityService.incrementShare(postId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @Operation(summary = "좋아요 토글")
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable("postId") Long postId
    ) {
        boolean liked = communityService.toggleLike(postId);
        Long likeCount = communityService.getLikeCount(postId);
        return ResponseEntity.ok(Map.of("liked", liked, "likeCount", likeCount));
    }

    @Operation(summary = "좋아요 상태 조회")
    @GetMapping("/posts/{postId}/like-status")
    public ResponseEntity<?> getLikeStatus(
            @PathVariable("postId") Long postId
    ) {
        boolean liked = communityService.isLiked(postId);
        Long likeCount = communityService.getLikeCount(postId);
        return ResponseEntity.ok(Map.of("liked", liked, "likeCount", likeCount));
    }

    @Operation(summary = "이미지 업로드")
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {
        Long imageId = communityService.uploadImage(file);
        return ResponseEntity.ok(Map.of("imageId", imageId));
    }

    @Operation(summary = "이미지 조회")
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable("id") Long id
    ) {
        byte[] imageData = communityService.getImage(id);
        String contentType = communityService.getImageContentType(id);
        String fileName = communityService.getImageName(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(imageData);
    }

    @Operation(summary = "수정용 데이터 조회")
    @GetMapping("/posts/{postId}/edit")
    public ResponseEntity<CommunityResponse> getPostForEdit(
            @PathVariable("postId") Long postId
    ) {
        return ResponseEntity.ok(communityService.getPost(postId));
    }

    @Operation(summary = "게시글 수정")
    @PutMapping("/posts/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable("postId") Long postId,
            @RequestBody CommunityRequest request
    ) {
        try {
            communityService.updatePost(postId, request);
            return ResponseEntity.ok(Map.of("success", true, "message", "게시글이 수정되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "서버 오류"));
        }
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable("postId") Long postId
    ) {
        try {
            communityService.deletePost(postId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "삭제 실패"));
        }
    }

    @Operation(summary = "댓글 작성")
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable("postId") Long postId,
            @RequestParam("userId") Long userId,
            @Valid @RequestBody CommunityCommentRequest request
    ) {
        communityService.createComment(postId, userId, request);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @Operation(summary = "대댓글 작성")
    @PostMapping("/posts/{postId}/comments/{parentId}")
    public ResponseEntity<?> createReply(
            @PathVariable("postId") Long postId,
            @PathVariable("parentId") Long parentId,
            @RequestParam("userId") Long userId,
            @Valid @RequestBody CommunityCommentRequest request
    ) {
        communityService.createReply(postId, parentId, userId, request);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @Operation(summary = "댓글 조회")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<CommunityCommentResponse> getComments(
            @PathVariable("postId") Long postId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(communityService.getComments(postId, page, size));
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("commentId") Long commentId,
            @RequestParam("userId") Long userId
    ) {
        communityService.deleteComment(commentId, userId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}