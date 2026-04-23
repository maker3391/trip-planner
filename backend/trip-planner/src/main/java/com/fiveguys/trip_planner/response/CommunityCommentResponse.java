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
        private String content;
        private String author;
        private LocalDateTime createdAt;
        private List<CommentDto> children;

        public CommentDto(CommunityComment parent, List<CommunityComment> children) {
            this.id = parent.getId();
            this.content = parent.getComment();
            this.author = parent.getUser().getNickname();
            this.createdAt = parent.getCreatedAt();
            this.children = children.stream()
                    .map(child -> new CommentDto(child, List.of()))
                    .toList();
        }
    }
}