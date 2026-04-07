package com.fiveguys.trip_planner.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 채팅 응답 객체")
public class ChatResponse {

    @Schema(description = "사용자가 보낸 원본 메시지", example = "부산 2박 3일 여행 코스 추천해줘")
    private String originalMessage;

    @Schema(description = "분석된 사용자의 의도 (예: 일정추천, 장소검색 등)", example = "TRAVEL_PLAN")
    private String intent;

    @Schema(description = "분석된 여행지", example = "부산")
    private String destination;

    @Schema(description = "분석된 여행 기간 (일 수)", example = "3")
    private Integer days;

    @Schema(description = "AI가 생성한 상세 추천 콘텐츠 (일정 및 장소 포함)")
    private RecommendationContentResponse recommendation;

    public ChatResponse() {
    }

    public ChatResponse(String originalMessage,
                        String intent,
                        String destination,
                        Integer days,
                        RecommendationContentResponse recommendation) {
        this.originalMessage = originalMessage;
        this.intent = intent;
        this.destination = destination;
        this.days = days;
        this.recommendation = recommendation;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public RecommendationContentResponse getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(RecommendationContentResponse recommendation) {
        this.recommendation = recommendation;
    }
}