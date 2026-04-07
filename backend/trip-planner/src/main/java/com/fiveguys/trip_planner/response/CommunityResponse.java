package com.fiveguys.trip_planner.response;

import com.fiveguys.trip_planner.entity.Community;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CommunityResponse {

    private Long id;
    private String category;
    private String region;
    private String title;
    private String content;

    private String departure;
    private String arrival;

    private String tags;
    private Integer rating;

    private LocalDateTime createdAt;

    /**
     * Entity → DTO 변환
     */
    public static CommunityResponse from(Community community) {
        return CommunityResponse.builder()
                .id(community.getId())
                .category(community.getCategory())
                .region(community.getRegion())
                .title(community.getTitle())
                .content(community.getContent())
                .departure(community.getDeparture())
                .arrival(community.getArrival())
                .tags(community.getTags())
                .rating(community.getRating())
                .createdAt(community.getCreatedAt())
                .build();
    }
}