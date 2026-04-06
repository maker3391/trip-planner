package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.DayPlanDraft;
import com.fiveguys.trip_planner.dto.RecommendationDraft;
import com.fiveguys.trip_planner.dto.RecommendationItemDraft;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RecommendationNormalizationService {

    private static final int MAX_PLACES_PER_DAY = 4;
    private static final int MAX_ITEMS = 5;

    public RecommendationDraft normalize(RecommendationDraft draft) {
        RecommendationDraft normalized = new RecommendationDraft();
        normalized.setIntent(trimToNull(draft.getIntent()));
        normalized.setDestination(trimToNull(draft.getDestination()));
        normalized.setDays(draft.getDays());

        if ("TRAVEL_ITINERARY".equals(draft.getIntent())) {
            normalized.setDayPlans(normalizeDayPlans(draft.getDayPlans(), draft.getDays()));
            normalized.setItems(new ArrayList<>());
        } else {
            normalized.setDayPlans(new ArrayList<>());
            normalized.setItems(normalizeItems(draft.getItems()));
            normalized.setDays(null);
        }

        return normalized;
    }

    private List<DayPlanDraft> normalizeDayPlans(List<DayPlanDraft> dayPlans, Integer days) {
        List<DayPlanDraft> result = new ArrayList<>();
        if (dayPlans == null || dayPlans.isEmpty() || days == null || days < 1) {
            return result;
        }

        for (int i = 0; i < dayPlans.size() && i < days; i++) {
            DayPlanDraft source = dayPlans.get(i);
            DayPlanDraft target = new DayPlanDraft();
            target.setDay(i + 1);
            target.setPlaces(normalizePlaces(source.getPlaces()));
            result.add(target);
        }

        return result;
    }

    private List<String> normalizePlaces(List<String> places) {
        List<String> result = new ArrayList<>();
        if (places == null || places.isEmpty()) {
            return result;
        }

        Set<String> dedup = new LinkedHashSet<>();
        for (String place : places) {
            String normalized = trimToNull(place);
            if (normalized != null) {
                dedup.add(normalized);
            }
            if (dedup.size() >= MAX_PLACES_PER_DAY) {
                break;
            }
        }

        result.addAll(dedup);
        return result;
    }

    private List<RecommendationItemDraft> normalizeItems(List<RecommendationItemDraft> items) {
        List<RecommendationItemDraft> result = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return result;
        }

        Set<String> dedupNames = new LinkedHashSet<>();
        for (RecommendationItemDraft item : items) {
            if (item == null) {
                continue;
            }

            String name = trimToNull(item.getName());
            if (name == null || dedupNames.contains(name)) {
                continue;
            }

            result.add(new RecommendationItemDraft(name));
            dedupNames.add(name);

            if (result.size() >= MAX_ITEMS) {
                break;
            }
        }

        return result;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}