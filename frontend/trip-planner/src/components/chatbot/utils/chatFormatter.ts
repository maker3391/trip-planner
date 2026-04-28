import type {
  ChatResponse,
  RecommendationItem,
  DayPlanResponse,
} from "../../api/chat";
import type {
  ChatMessage,
  RecommendationCardItem,
  RecommendationKind,
  RecommendationPayload,
} from "../types/chatUi";

export interface FormattedChatResponse {
  content: string;
  variant: ChatMessage["variant"];
  payload?: RecommendationPayload;
}

const safeText = (value?: string | null): string => {
  return value?.trim() ?? "";
};

const isNonEmptyArray = <T>(value?: T[] | null): value is T[] => {
  return Array.isArray(value) && value.length > 0;
};

const normalizeDestinationLabel = (rawDestination?: string | null): string => {
  const destination = safeText(rawDestination);
  if (!destination) return "";

  const parts = destination.split(/\s+/).filter(Boolean);
  const normalizedParts: string[] = [];

  for (const part of parts) {
    if (normalizedParts[normalizedParts.length - 1] !== part) {
      normalizedParts.push(part);
    }
  }

  return normalizedParts.join(" ");
};

const pickRecommendationItems = (
  data: ChatResponse
): RecommendationItem[] => {
  return (
    data.recommendation?.items ||
    data.recommendation?.recommendations ||
    data.recommendation?.places ||
    data.recommendation?.restaurants ||
    data.recommendation?.hotels ||
    []
  );
};

const formatRecommendationItems = (
  items?: RecommendationItem[],
  title?: string
): string => {
  if (!items || items.length === 0) return "";

  const lines: string[] = [];

  if (title) {
    lines.push(title);
    lines.push("");
  }

  items.forEach((item, index) => {
    const name =
      safeText(item.title) || safeText(item.name) || `추천 ${index + 1}`;
    const category = safeText(item.category);
    const description =
      safeText(item.description) ||
      safeText(item.content) ||
      safeText(item.address);

    lines.push(`${index + 1}. ${name}${category ? ` (${category})` : ""}`);

    if (description) {
      lines.push(`- ${description}`);
    }

    if (index !== items.length - 1) {
      lines.push("");
    }
  });

  return lines.join("\n").trim();
};

const formatTripDuration = (days?: number): string => {
  if (!days || days <= 0) return "";

  if (days === 1) return "당일 일정";

  const nights = days - 1;
  return `${nights}박 ${days}일 일정`;
};

const formatItineraryHeader = (data: ChatResponse): string => {
  const destination = normalizeDestinationLabel(data.destination);
  const duration = formatTripDuration(data.days);

  if (destination && duration) return `${destination} · ${duration}`;
  if (destination) return `${destination} 여행 일정`;
  if (duration) return duration;
  return "여행 일정";
};

const formatCompactDayPlanBlock = (dayPlan: DayPlanResponse): string[] => {
  const places = (dayPlan.places ?? [])
    .map((place) => safeText(place))
    .filter(Boolean);

  const lines: string[] = [];
  lines.push(`Day ${dayPlan.day}`);

  if (places.length > 0) {
    lines.push(places.join(" → "));
  }

  return lines;
};

const formatItineraryLines = (
  title: string,
  dayPlans?: DayPlanResponse[],
  fallbackSummary?: string
): string[] => {
  const lines: string[] = [];

  lines.push(title);
  lines.push("");

  if (isNonEmptyArray(dayPlans)) {
    dayPlans
      .slice()
      .sort((a, b) => a.day - b.day)
      .forEach((dayPlan, index) => {
        lines.push(...formatCompactDayPlanBlock(dayPlan));

        if (index !== dayPlans.length - 1) {
          lines.push("");
        }
      });
  } else if (fallbackSummary) {
    lines.push(fallbackSummary);
  } else {
    lines.push("추천 일정을 정리하지 못했습니다.");
  }

  return lines;
};

const formatItineraryResponse = (data: ChatResponse): FormattedChatResponse => {
  const dayPlans = data.recommendation?.dayPlans ?? [];
  const lines = formatItineraryLines(
    formatItineraryHeader(data),
    dayPlans,
    safeText(data.recommendation?.summary)
  );

  return {
    content: lines.join("\n").trim(),
    variant: "itinerary",
  };
};

const normalizeRecommendationCardItems = (
  items: RecommendationItem[],
): RecommendationCardItem[] => {
  return items.map((item, index) => ({
    id: `${safeText(item.name) || safeText(item.title) || "recommendation"}-${index}`,
    title: safeText(item.name) || safeText(item.title) || `추천 ${index + 1}`,
    category: safeText(item.category),
    description: safeText(item.description) || safeText(item.content),
    address: safeText(item.address),
    link: safeText(item.placeUrl) || safeText(item.link),
  }));
};

const buildFallbackRecommendationTitle = (
  destination: string,
  kind: RecommendationKind
): string => {
  const region = normalizeDestinationLabel(destination) || "이 지역";

  if (kind === "restaurant") {
    return `${region} 추천 정보를 모아봤어요`;
  }

  if (kind === "stay") {
    return `${region} 숙소 정보를 모아봤어요`;
  }

  return `${region}에서 가볼 만한 명소를 모아봤어요`;
};

const createRecommendationPayload = (
  kind: RecommendationKind,
  rawItems: RecommendationItem[],
  title: string,
  summary = ""
): RecommendationPayload => {
  const normalizedItems = normalizeRecommendationCardItems(rawItems);

  return {
    kind,
    title,
    summary,
    items: normalizedItems,
  };
};

const createRecommendationFormattedResponse = (
  kind: RecommendationKind,
  rawItems: RecommendationItem[],
  title: string,
  summary = ""
): FormattedChatResponse => {
  const payload = createRecommendationPayload(kind, rawItems, title, summary);

  const fallbackTextLines: string[] = [];
  fallbackTextLines.push(payload.title);

  if (summary) {
    fallbackTextLines.push("");
    fallbackTextLines.push(summary);
  }

  if (payload.items.length > 0) {
    fallbackTextLines.push("");
    payload.items.forEach((item, index) => {
      fallbackTextLines.push(
        `${index + 1}. ${item.title}${item.category ? ` (${item.category})` : ""}`
      );

      if (item.address) {
        fallbackTextLines.push(`- ${item.address}`);
      }

      if (index !== payload.items.length - 1) {
        fallbackTextLines.push("");
      }
    });
  }

  return {
    content:
      fallbackTextLines.join("\n").trim() ||
      (kind === "restaurant"
        ? "추천할 맛집 정보를 찾지 못했습니다."
        : "추천할 숙소 정보를 찾지 못했습니다."),
    variant: "recommendation",
    payload,
  };
};

const formatRecommendationResponse = (
  data: ChatResponse,
  kind: RecommendationKind
): FormattedChatResponse => {
  const summary =
    safeText(data.recommendation?.summary) ||
    safeText(data.combinedRecommendation?.summary);

  const title =
    safeText(data.recommendation?.displayTitle) ||
    buildFallbackRecommendationTitle(safeText(data.destination), kind);

  return createRecommendationFormattedResponse(
    kind,
    pickRecommendationItems(data),
    title,
    summary
  );
};

const formatCombinedResponses = (
  data: ChatResponse
): FormattedChatResponse[] => {
  const combined = data.combinedRecommendation;
  const responses: FormattedChatResponse[] = [];

  const itineraryDayPlans = combined?.itinerary?.dayPlans ?? [];
  if (isNonEmptyArray(itineraryDayPlans)) {
    responses.push({
      content: formatItineraryLines(
        formatItineraryHeader(data),
        itineraryDayPlans,
        safeText(combined?.summary)
      )
        .join("\n")
        .trim(),
      variant: "itinerary",
    });
  }

  if (isNonEmptyArray(combined?.restaurants)) {
    responses.push(
      createRecommendationFormattedResponse(
        "restaurant",
        combined.restaurants,
        safeText(combined.restaurantDisplayTitle) ||
          buildFallbackRecommendationTitle(
            safeText(data.destination),
            "restaurant"
          )
      )
    );
  }

  if (isNonEmptyArray(combined?.stays)) {
    responses.push(
      createRecommendationFormattedResponse(
        "stay",
        combined.stays,
        safeText(combined.stayDisplayTitle) ||
          buildFallbackRecommendationTitle(safeText(data.destination), "stay")
      )
    );
  }

  if (isNonEmptyArray(combined?.attractions)) {
    responses.push(
      createRecommendationFormattedResponse(
        "attraction",
        combined.attractions,
        safeText(combined.attractionDisplayTitle) ||
          buildFallbackRecommendationTitle(
            safeText(data.destination),
            "attraction"
          )
      )
    );
  }

  if (responses.length > 0) {
    return responses;
  }

  return [
    {
      content: "복합 추천 결과를 정리하지 못했습니다.",
      variant: "default",
    },
  ];
};

const inferIntent = (data: ChatResponse): string => {
  if (data.intent) return data.intent;

  if (isNonEmptyArray(data.recommendation?.dayPlans)) {
    return "TRAVEL_ITINERARY";
  }

  if (isNonEmptyArray(data.recommendation?.restaurants)) {
    return "RESTAURANT_RECOMMENDATION";
  }

  if (isNonEmptyArray(data.recommendation?.hotels)) {
    return "STAY_RECOMMENDATION";
  }

  if (data.combinedRecommendation) {
    return "COMBINED_RECOMMENDATION";
  }

  return "UNKNOWN";
};

export const formatChatResponses = (
  data: ChatResponse
): FormattedChatResponse[] => {
  const intent = inferIntent(data);

  switch (intent) {
    case "TRAVEL_ITINERARY":
      return [formatItineraryResponse(data)];

    case "RESTAURANT_RECOMMENDATION":
      return [formatRecommendationResponse(data, "restaurant")];

    case "STAY_RECOMMENDATION":
      return [formatRecommendationResponse(data, "stay")];

    case "ATTRACTION_RECOMMENDATION":
      return [formatRecommendationResponse(data, "attraction")];

    case "COMBINED_RECOMMENDATION":
      return formatCombinedResponses(data);

    default: {
      const summary =
        safeText(data.recommendation?.summary) ||
        safeText(data.combinedRecommendation?.summary);

      const dayPlans = data.recommendation?.dayPlans ?? [];
      if (isNonEmptyArray(dayPlans)) {
        return [formatItineraryResponse(data)];
      }

      const items = pickRecommendationItems(data);
      const itemText = formatRecommendationItems(items);

      if (summary && itemText) {
        return [
          {
            content: `${summary}\n\n${itemText}`.trim(),
            variant: "default",
          },
        ];
      }

      if (summary) {
        return [
          {
            content: summary,
            variant: "default",
          },
        ];
      }

      if (itemText) {
        return [
          {
            content: itemText,
            variant: "default",
          },
        ];
      }

      return [
        {
          content: "응답은 받았지만 표시할 내용이 없습니다.",
          variant: "default",
        },
      ];
    }
  }
};