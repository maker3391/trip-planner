package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.CommunityRequest;
import com.fiveguys.trip_planner.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

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
}