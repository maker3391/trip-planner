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

const stripRecommendationSuffix = (message?: string | null): string => {
  const text = safeText(message);
  if (!text) return "";

  return text
    .replace(/\s*(맛집|숙소|숙박|호텔|모텔|펜션|리조트|게스트하우스|한옥스테이)\s*추천\s*$/u, "")
    .replace(/\s*여행\s*추천\s*$/u, "")
    .trim();
};

const extractRequestedAreaLabel = (originalMessage?: string | null): string => {
  const base = stripRecommendationSuffix(originalMessage);
  if (!base) return "";

  const parts = base.split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "";

  const priorityPatterns = [/(동|읍|면|리)$/u, /(구|군)$/u, /시$/u];

  for (const pattern of priorityPatterns) {
    for (let i = parts.length - 1; i >= 0; i -= 1) {
      if (pattern.test(parts[i])) {
        return parts[i];
      }
    }
  }

  return parts[parts.length - 1] ?? "";
};

const extractStayLabel = (originalMessage?: string | null): string => {
  const text = safeText(originalMessage);

  if (!text) return "숙소";

  if (text.includes("게스트하우스")) return "게스트하우스";
  if (text.includes("한옥스테이")) return "한옥스테이";
  if (text.includes("리조트")) return "리조트";
  if (text.includes("펜션")) return "펜션";
  if (text.includes("호텔")) return "호텔";
  if (text.includes("모텔")) return "모텔";
  if (text.includes("숙박")) return "숙소";
  if (text.includes("숙소")) return "숙소";

  return "숙소";
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
    data.combinedRecommendation?.items ||
    data.combinedRecommendation?.recommendations ||
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

const formatItineraryResponse = (data: ChatResponse): FormattedChatResponse => {
  const dayPlans = data.recommendation?.dayPlans ?? [];
  const lines: string[] = [];

  lines.push(formatItineraryHeader(data));
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
  } else if (safeText(data.combinedRecommendation?.itinerary)) {
    lines.push(safeText(data.combinedRecommendation?.itinerary));
  } else if (safeText(data.recommendation?.summary)) {
    lines.push(safeText(data.recommendation?.summary));
  } else {
    lines.push("추천 일정을 정리하지 못했습니다.");
  }

  return {
    content: lines.join("\n").trim(),
    variant: "itinerary",
  };
};

const buildRecommendationTitle = (
  originalMessage: string,
  destination: string,
  kind: RecommendationKind
): string => {
  const requestedAreaLabel = extractRequestedAreaLabel(originalMessage);
  const normalizedDestination = normalizeDestinationLabel(destination);
  const areaLabel = requestedAreaLabel || normalizedDestination;

  if (!areaLabel) {
    if (kind === "restaurant") return "맛집 추천";
    return `${extractStayLabel(originalMessage)} 추천`;
  }

  if (kind === "restaurant") {
    return `${areaLabel} 맛집 추천`;
  }

  return `${areaLabel} ${extractStayLabel(originalMessage)} 추천`;
};

const extractCompactCategory = (rawCategory?: string | null): string => {
  const category = safeText(rawCategory);

  if (!category) return "";

  const parts = category
    .split(">")
    .map((part) => part.trim())
    .filter(Boolean);

  if (parts.length === 0) return "";

  return parts[parts.length - 1] ?? "";
};

const normalizeRecommendationCardItems = (
  items: RecommendationItem[]
): RecommendationCardItem[] => {
  return items.map((item, index) => ({
    id: `${safeText(item.name) || safeText(item.title) || "recommendation"}-${index}`,
    title: safeText(item.name) || safeText(item.title) || `추천 ${index + 1}`,
    category: extractCompactCategory(item.category),
    description: safeText(item.description) || safeText(item.content),
    address: safeText(item.address),
    link: safeText(item.placeUrl) || safeText(item.link),
  }));
};

const formatRecommendationResponse = (
  data: ChatResponse,
  kind: RecommendationKind
): FormattedChatResponse => {
  const summary =
    safeText(data.recommendation?.summary) ||
    safeText(data.combinedRecommendation?.summary);

  const items = pickRecommendationItems(data);
  const normalizedItems = normalizeRecommendationCardItems(items);
  const title = buildRecommendationTitle(
    safeText(data.originalMessage),
    safeText(data.destination),
    kind
  );

  const fallbackTextLines: string[] = [];
  fallbackTextLines.push(title);

  if (summary) {
    fallbackTextLines.push("");
    fallbackTextLines.push(summary);
  }

  if (normalizedItems.length > 0) {
    fallbackTextLines.push("");
    normalizedItems.forEach((item, index) => {
      fallbackTextLines.push(
        `${index + 1}. ${item.title}${item.category ? ` (${item.category})` : ""}`
      );

      if (item.address) {
        fallbackTextLines.push(`- ${item.address}`);
      }

      if (index !== normalizedItems.length - 1) {
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
    payload: {
      kind,
      title,
      summary,
      items: normalizedItems,
    },
  };
};

const formatCombinedResponse = (data: ChatResponse): FormattedChatResponse => {
  const lines: string[] = [];
  const summary = safeText(data.combinedRecommendation?.summary);
  const itinerary = safeText(data.combinedRecommendation?.itinerary);
  const items = pickRecommendationItems(data);

  lines.push(formatItineraryHeader(data));
  lines.push("");

  if (summary) {
    lines.push(summary);
    lines.push("");
  }

  if (itinerary) {
    lines.push(itinerary);
    lines.push("");
  }

  const itemText = formatRecommendationItems(items, "추천 장소");
  if (itemText) {
    lines.push(itemText);
  }

  return {
    content: lines.join("\n").trim() || "복합 추천 결과를 정리하지 못했습니다.",
    variant: "default",
  };
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

export const formatChatResponse = (
  data: ChatResponse
): FormattedChatResponse => {
  const intent = inferIntent(data);

  switch (intent) {
    case "TRAVEL_ITINERARY":
      return formatItineraryResponse(data);

    case "RESTAURANT_RECOMMENDATION":
      return formatRecommendationResponse(data, "restaurant");

    case "STAY_RECOMMENDATION":
      return formatRecommendationResponse(data, "stay");

    case "COMBINED_RECOMMENDATION":
      return formatCombinedResponse(data);

    default: {
      const summary =
        safeText(data.recommendation?.summary) ||
        safeText(data.combinedRecommendation?.summary);

      const dayPlans = data.recommendation?.dayPlans ?? [];
      if (isNonEmptyArray(dayPlans)) {
        return formatItineraryResponse(data);
      }

      const items = pickRecommendationItems(data);
      const itemText = formatRecommendationItems(items);

      if (summary && itemText) {
        return {
          content: `${summary}\n\n${itemText}`.trim(),
          variant: "default",
        };
      }

      if (summary) {
        return {
          content: summary,
          variant: "default",
        };
      }

      if (itemText) {
        return {
          content: itemText,
          variant: "default",
        };
      }

      return {
        content: "응답은 받았지만 표시할 내용이 없습니다.",
        variant: "default",
      };
    }
  }
};