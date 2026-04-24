package com.fiveguys.trip_planner.response;

import com.fiveguys.trip_planner.entity.CommunityComment;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class CommunityCommentResponse {

    private List<CommentDto> comments;
    private int totalPages;

    @Getter
    public static class CommentDto {

        private Long id;
        private Long userId;       // 🔥 프론트엔드 본인 확인용 (삭제 버튼 노출에 필요)
        private String nickname;   // 🔥 프론트엔드 작성자 이름 렌더링용 (기존 author -> nickname으로 변경)
        private String content;
        private LocalDateTime createdAt;
        private boolean isDeleted; // 🔥 프론트엔드 논리적 삭제 처리용
        private List<CommentDto> children;

        public CommentDto(CommunityComment parent, List<CommunityComment> children) {
            this.id = parent.getId();
            this.userId = parent.getUser().getId();             // 최신 User 객체에서 ID 추출
            this.nickname = parent.getUser().getNickname();     // 최신 User 객체에서 닉네임 추출
            this.content = parent.getComment();
            this.createdAt = parent.getCreatedAt();
            this.isDeleted = parent.isDeleted();                // 삭제 여부 상태 추가

            // 자식 댓글(대댓글) 변환
            // parent.getChildren()을 통해 대댓글 리스트를 넘겨받아 재귀적으로 DTO로 변환합니다.
            this.children = children.stream()
                    // 대댓글의 자식은 보통 없으므로(2 depth 구조) 빈 리스트로 넘기거나 child.getChildren()을 넘깁니다.
                    .map(child -> new CommentDto(child, child.getChildren()))
                    .toList();
        }
    }
}