package com.fiveguys.trip_planner.response;

public class ChatResponse {

    private String originalMessage;
    private String intent;
    private String destination;
    private Integer days;
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