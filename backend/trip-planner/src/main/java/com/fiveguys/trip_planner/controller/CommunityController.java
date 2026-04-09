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

@Tag(name = "💬 커뮤니티 API", description = "여행 정보 공유 및 게시글 관리를 담당합니다.")
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    // 🔹 게시글 생성
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody CommunityRequest request) {
        try {
            Long postId = communityService.createPost(request);
            return ResponseEntity.status(201).body(
                    Map.of(
                            "success", true,
                            "postId", postId,
                            "message", "게시글이 성공적으로 등록되었습니다."
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "success", false,
                            "message", "서버 오류가 발생했습니다."
                    )
            );
        }
    }

    // 🔹 게시글 목록 조회
    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<CommunityResponse> postPage = communityService.getPosts(page, size);
            return ResponseEntity.ok(postPage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "success", false,
                            "message", "게시글 조회 중 서버 오류가 발생했습니다."
                    )
            );
        }
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> getPost(
            @PathVariable("postId") Long postId
    ) {
        return ResponseEntity.ok(communityService.getPost(postId));
    }

    @PatchMapping("posts/{postId}/view")
    public ResponseEntity<?> viewPost(@PathVariable("postId") Long postId) {
        try {
            communityService.viewPost(postId);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "조회수가 증가했습니다."
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "success", false,
                            "message", "게시글 조회 처리 중 서버 오류가 발생했습니다."
                    )
            );
        }
    }

    // 🔹 좋아요 증가
    @PatchMapping("/posts/{postId}/recommend")
    public ResponseEntity<?> recommendPost(@PathVariable Long postId) {
        try {
            communityService.recommendPost(postId);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "좋아요가 증가했습니다."
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "success", false,
                            "message", "좋아요 처리 중 서버 오류가 발생했습니다."
                    )
            );
        }
    }

    // 🔹 좋아요 감소
    @PatchMapping("/posts/{postId}/unrecommend")
    public ResponseEntity<?> unRecommendPost(@PathVariable Long postId) {
        try {
            communityService.unRecommendPost(postId);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "좋아요가 감소했습니다."
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "success", false,
                            "message", "좋아요 처리 중 서버 오류가 발생했습니다."
                    )
            );
        }
    }

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {

        Long imageId = communityService.uploadImage(file);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "imageId", imageId
                )
        );
    }

    // 🔥 이미지 조회
    // 🔥 이미지 조회 API 수정
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        // 직접 레포지토리를 부르는 대신 서비스에 위임합니다.
        CommunityImage image = communityService.getImageEntity(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getOriginalName() + "\"")
                .body(image.getData());
    }
}