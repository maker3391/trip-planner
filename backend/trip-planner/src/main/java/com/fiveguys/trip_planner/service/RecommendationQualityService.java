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
public class RecommendationQualityService {

    private static final int MAX_PLACES_PER_DAY = 4;
    private static final int MAX_ITEMS = 5;

    private final DetailAreaParsingService detailAreaParsingService;

    public RecommendationQualityService(DetailAreaParsingService detailAreaParsingService) {
        this.detailAreaParsingService = detailAreaParsingService;
    }

    public RecommendationDraft adjust(String originalMessage, RecommendationDraft draft) {
        if (draft == null) {
            return null;
        }

        String detailArea = resolveDetailArea(originalMessage, draft.getDetailArea(), draft.getDestination());

        RecommendationDraft adjusted = new RecommendationDraft();
        adjusted.setIntent(draft.getIntent());
        adjusted.setDestination(buildDisplayDestination(draft.getDestination(), detailArea));
        adjusted.setDetailArea(detailArea);
        adjusted.setDays(draft.getDays());

        if ("TRAVEL_ITINERARY".equals(draft.getIntent())) {
            adjusted.setDayPlans(adjustDayPlans(draft.getDayPlans(), draft.getDays(), detailArea));
            adjusted.setItems(new ArrayList<>());
        } else {
            adjusted.setDayPlans(new ArrayList<>());
            adjusted.setItems(adjustItems(draft.getItems(), detailArea));
            adjusted.setDays(null);
        }

        return adjusted;
    }

    private List<DayPlanDraft> adjustDayPlans(List<DayPlanDraft> dayPlans, Integer days, String detailArea) {
        List<DayPlanDraft> result = new ArrayList<>();
        if (dayPlans == null || dayPlans.isEmpty() || days == null || days < 1) {
            return result;
        }

        Set<String> globalSeen = new LinkedHashSet<>();

        for (int i = 0; i < dayPlans.size() && i < days; i++) {
            DayPlanDraft source = dayPlans.get(i);
            DayPlanDraft target = new DayPlanDraft();
            target.setDay(i + 1);
            target.setPlaces(adjustPlaces(source == null ? null : source.getPlaces(), detailArea, globalSeen));
            result.add(target);
        }

        return result;
    }

    private List<String> adjustPlaces(List<String> places, String detailArea, Set<String> globalSeen) {
        List<String> result = new ArrayList<>();
        if (places == null || places.isEmpty()) {
            return result;
        }

        Set<String> daySeen = new LinkedHashSet<>();
        List<String> nearbyMatched = new ArrayList<>();
        List<String> others = new ArrayList<>();

        for (String place : places) {
            String cleaned = cleanPlaceName(place);
            if (!StringUtils.hasText(cleaned)) {
                continue;
            }

            if (looksLikeGeneratedDescription(cleaned)) {
                continue;
            }

            String signature = buildSignature(cleaned);

            if (daySeen.contains(signature) || globalSeen.contains(signature)) {
                continue;
            }

            if (detailAreaParsingService.matchesNearby(detailArea, cleaned)) {
                nearbyMatched.add(cleaned);
            } else {
                others.add(cleaned);
            }

            daySeen.add(signature);
            globalSeen.add(signature);
        }

        for (String place : nearbyMatched) {
            result.add(place);
            if (result.size() >= MAX_PLACES_PER_DAY) {
                return result;
            }
        }

        for (String place : others) {
            result.add(place);
            if (result.size() >= MAX_PLACES_PER_DAY) {
                return result;
            }
        }

        return result;
    }

    private List<RecommendationItemDraft> adjustItems(List<RecommendationItemDraft> items, String detailArea) {
        List<RecommendationItemDraft> result = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return result;
        }

        Set<String> seen = new LinkedHashSet<>();
        List<String> nearbyMatched = new ArrayList<>();
        List<String> others = new ArrayList<>();

        for (RecommendationItemDraft item : items) {
            if (item == null) {
                continue;
            }

            String cleaned = cleanItemName(item.getName());
            if (!StringUtils.hasText(cleaned)) {
                continue;
            }

            if (looksLikeGeneratedDescription(cleaned)) {
                continue;
            }

            String signature = buildSignature(cleaned);
            if (seen.contains(signature)) {
                continue;
            }

            if (detailAreaParsingService.matchesNearby(detailArea, cleaned)) {
                nearbyMatched.add(cleaned);
            } else {
                others.add(cleaned);
            }

            seen.add(signature);
        }

        for (String value : nearbyMatched) {
            result.add(new RecommendationItemDraft(value));
            if (result.size() >= MAX_ITEMS) {
                return result;
            }
        }

        for (String value : others) {
            result.add(new RecommendationItemDraft(value));
            if (result.size() >= MAX_ITEMS) {
                return result;
            }
        }

        return result;
    }

    private String resolveDetailArea(String originalMessage, String draftDetailArea, String destination) {
        if (StringUtils.hasText(draftDetailArea)) {
            String extracted = detailAreaParsingService.extractDetailArea(draftDetailArea);
            if (StringUtils.hasText(extracted)) {
                return extracted;
            }
        }

        if (StringUtils.hasText(destination)) {
            String extracted = detailAreaParsingService.extractDetailArea(destination);
            if (StringUtils.hasText(extracted)) {
                return extracted;
            }
        }

        return detailAreaParsingService.extractDetailArea(originalMessage);
    }

    private String buildDisplayDestination(String destination, String detailArea) {
        if (!StringUtils.hasText(detailArea)) {
            return destination;
        }
        if (!StringUtils.hasText(destination)) {
            return detailArea;
        }
        return destination + " " + detailArea;
    }

    private String cleanPlaceName(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String cleaned = value.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("^[0-9]+[.)]\\s*", "")
                .replaceAll("^(추천|방문|코스|일정)\\s*", "")
                .trim();

        if (!StringUtils.hasText(cleaned)) {
            return null;
        }

        if (cleaned.length() > 40) {
            return null;
        }

        if (cleaned.endsWith("일정") || cleaned.endsWith("코스") || cleaned.endsWith("여행")) {
            return null;
        }

        return cleaned;
    }

    private String cleanItemName(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String cleaned = value.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("^[0-9]+[.)]\\s*", "")
                .replaceAll("^(추천|방문|코스|일정)\\s*", "")
                .trim();

        if (!StringUtils.hasText(cleaned)) {
            return null;
        }

        if (cleaned.length() > 30) {
            return null;
        }

        return cleaned;
    }

    private boolean looksLikeGeneratedDescription(String name) {
        String value = name.trim();

        if (value.length() > 25) {
            return true;
        }

        if (value.contains("대표")
                || value.contains("핫플")
                || value.contains("감성")
                || value.contains("분위기")
                || value.contains("로컬")
                || value.contains("추천")
                || value.contains("명소")
                || value.contains("스팟")
                || value.contains("거리")
                || value.contains("코스")) {
            return true;
        }

        if (value.contains("의 ")) {
            return true;
        }

        return false;
    }

    private String buildSignature(String value) {
        return value.toLowerCase()
                .replaceAll("[\\s\\-_/()\\[\\],.]", "");
    }
}