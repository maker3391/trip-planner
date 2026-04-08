package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.CommunityRequest;
import com.fiveguys.trip_planner.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "💬 커뮤니티 API", description = "여행 정보 공유 및 게시글 관리를 담당합니다.")
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "게시글 등록", description = "제목, 내용, 카테고리 등을 입력받아 커뮤니티에 새로운 글을 작성합니다.")
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

    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            // Service에서 Page 반환
            var postPage = communityService.getPosts(page, size);

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
}