package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.DayPlanDraft;
import com.fiveguys.trip_planner.dto.RecommendationDraft;
import com.fiveguys.trip_planner.dto.RecommendationItemDraft;
import com.fiveguys.trip_planner.exception.LlmCallException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
public class RecommendationValidationService {

    private static final Set<String> SUPPORTED_INTENTS = Set.of(
            "TRAVEL_ITINERARY",
            "RESTAURANT_RECOMMENDATION",
            "STAY_RECOMMENDATION"
    );

    public void validate(RecommendationDraft draft) {
        if (draft == null) {
            throw new LlmCallException("추천 결과가 비어 있습니다.");
        }

        if (!StringUtils.hasText(draft.getIntent())) {
            throw new LlmCallException("추천 유형이 비어 있습니다.");
        }

        if (!SUPPORTED_INTENTS.contains(draft.getIntent())) {
            throw new LlmCallException("지원하지 않는 추천 유형입니다.");
        }

        if (!StringUtils.hasText(draft.getDestination())) {
            throw new LlmCallException("추천 지역이 비어 있습니다.");
        }

        switch (draft.getIntent()) {
            case "TRAVEL_ITINERARY" -> validateItinerary(draft);
            case "RESTAURANT_RECOMMENDATION", "STAY_RECOMMENDATION" -> validateItemsOnly(draft);
            default -> throw new LlmCallException("지원하지 않는 추천 유형입니다.");
        }
    }

    private void validateItinerary(RecommendationDraft draft) {
        if (draft.getDays() == null || draft.getDays() < 1) {
            throw new LlmCallException("여행 일정 추천에는 여행 일수가 필요합니다.");
        }

        if (draft.getItems() != null && !draft.getItems().isEmpty()) {
            throw new LlmCallException("여행 일정 추천에서는 items가 비어 있어야 합니다.");
        }

        List<DayPlanDraft> dayPlans = draft.getDayPlans();
        if (dayPlans == null || dayPlans.isEmpty()) {
            throw new LlmCallException("여행 일정 추천 결과가 비어 있습니다.");
        }

        if (dayPlans.size() != draft.getDays()) {
            throw new LlmCallException("여행 일수와 일정 개수가 맞지 않습니다.");
        }

        for (DayPlanDraft dayPlan : dayPlans) {
            if (dayPlan == null || dayPlan.getDay() == null || dayPlan.getDay() < 1) {
                throw new LlmCallException("일차 정보가 올바르지 않습니다.");
            }

            if (dayPlan.getPlaces() == null || dayPlan.getPlaces().isEmpty()) {
                throw new LlmCallException(dayPlan.getDay() + "일차 추천 장소가 비어 있습니다.");
            }
        }
    }

    private void validateItemsOnly(RecommendationDraft draft) {
        if (draft.getItems() == null || draft.getItems().isEmpty()) {
            throw new LlmCallException("추천 결과가 비어 있습니다.");
        }

        for (RecommendationItemDraft item : draft.getItems()) {
            if (item == null || !StringUtils.hasText(item.getName())) {
                throw new LlmCallException("추천 항목 이름이 비어 있습니다.");
            }
        }
    }
}