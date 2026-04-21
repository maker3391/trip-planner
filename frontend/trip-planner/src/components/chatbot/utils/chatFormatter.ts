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

const RECOMMENDATION_TYPE_KEYWORDS = [
  "맛집",
  "숙소",
  "숙박",
  "호텔",
  "모텔",
  "펜션",
  "리조트",
  "게스트하우스",
  "한옥스테이",
  "풀빌라",
];

const RESTAURANT_GENERIC_CATEGORY_KEYWORDS = [
  "맛집",
  "음식점",
  "식당",
  "레스토랑",
  "음식",
];

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

  let stripped = text.replace(/\s*추천\s*$/u, "").trim();

  for (const keyword of RECOMMENDATION_TYPE_KEYWORDS) {
    const pattern = new RegExp(`\\s*${keyword}\\s*$`, "u");
    stripped = stripped.replace(pattern, "").trim();
  }

  stripped = stripped.replace(/\s*여행\s*$/u, "").trim();

  return stripped;
};

const isRecommendationTypeKeyword = (value?: string | null): boolean => {
  const text = safeText(value);
  if (!text) return false;

  return RECOMMENDATION_TYPE_KEYWORDS.includes(text);
};

const extractRequestedAreaLabel = (originalMessage?: string | null): string => {
  const base = stripRecommendationSuffix(originalMessage);
  if (!base) return "";

  const parts = base.split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "";

  const filteredParts = parts.filter(
    (part) => !isRecommendationTypeKeyword(part)
  );

  if (filteredParts.length === 0) return "";

  const priorityPatterns = [/(동|읍|면|리)$/u, /(구|군)$/u, /시$/u];

  for (const pattern of priorityPatterns) {
    for (let i = filteredParts.length - 1; i >= 0; i -= 1) {
      if (pattern.test(filteredParts[i])) {
        return filteredParts[i];
      }
    }
  }

  if (filteredParts.length >= 2) {
    return filteredParts.slice(-2).join(" ");
  }

  return filteredParts[filteredParts.length - 1] ?? "";
};

const splitCategoryParts = (rawCategory?: string | null): string[] => {
  const category = safeText(rawCategory);
  if (!category) return [];

  return category
    .split(">")
    .map((part) => part.trim())
    .filter(Boolean);
};

const extractCompactCategory = (rawCategory?: string | null): string => {
  const parts = splitCategoryParts(rawCategory);
  if (parts.length === 0) return "";

  return parts[parts.length - 1] ?? "";
};

const isRestaurantGenericCategory = (value?: string | null): boolean => {
  const text = safeText(value);
  if (!text) return true;

  return RESTAURANT_GENERIC_CATEGORY_KEYWORDS.includes(text);
};

const extractRestaurantDisplayCategory = (
  rawCategory?: string | null
): string => {
  const parts = splitCategoryParts(rawCategory);

  if (parts.length === 0) {
    return "";
  }

  const meaningfulParts = parts.filter(
    (part) => !isRestaurantGenericCategory(part)
  );

  if (meaningfulParts.length > 0) {
    return meaningfulParts[meaningfulParts.length - 1] ?? "";
  }

  return parts[parts.length - 1] ?? "";
};

const extractRequestedStayType = (originalMessage?: string | null): string => {
  const text = safeText(originalMessage);
  if (!text) return "";

  if (text.includes("풀빌라")) return "풀빌라";
  if (text.includes("한옥스테이")) return "한옥스테이";
  if (text.includes("게스트하우스")) return "게스트하우스";
  if (text.includes("리조트")) return "리조트";
  if (text.includes("펜션")) return "펜션";
  if (text.includes("무인텔") || text.includes("모텔")) return "모텔";
  if (text.includes("호텔")) return "호텔";
  if (text.includes("숙소") || text.includes("숙박")) return "숙소";

  return "";
};

const resolveStayDisplayType = (
  originalMessage: string,
  items: RecommendationCardItem[]
): string => {
  const requestedStayType = extractRequestedStayType(originalMessage);
  if (requestedStayType) {
    return requestedStayType;
  }

  const firstCategory = safeText(items[0]?.category);
  if (firstCategory) {
    return firstCategory;
  }

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
  kind: RecommendationKind
): RecommendationCardItem[] => {
  return items.map((item, index) => ({
    id: `${safeText(item.name) || safeText(item.title) || "recommendation"}-${index}`,
    title: safeText(item.name) || safeText(item.title) || `추천 ${index + 1}`,
    category:
      kind === "restaurant"
        ? extractRestaurantDisplayCategory(item.category)
        : extractCompactCategory(item.category),
    description: safeText(item.description) || safeText(item.content),
    address: safeText(item.address),
    link: safeText(item.placeUrl) || safeText(item.link),
  }));
};

const RESTAURANT_COMMENT_TEMPLATES = [
  "{region} 맛집을 모아봤어요",
  "{region}에서 가볼 만한 맛집을 정리했어요",
  "{region} 근처 맛집들을 추려봤어요",
  "{region}에서 들러볼 맛집을 모아봤어요",
];

const pickRandomTemplate = (templates: string[]): string => {
  if (templates.length === 0) {
    return "{region} 정보를 모아봤어요";
  }

  const index = Math.floor(Math.random() * templates.length);
  return templates[index] ?? templates[0];
};

const resolveRecommendationRegion = (
  originalMessage: string,
  destination: string
): string => {
  const requestedAreaLabel = extractRequestedAreaLabel(originalMessage);
  const normalizedDestination = normalizeDestinationLabel(destination);

  if (
    requestedAreaLabel &&
    !isRecommendationTypeKeyword(requestedAreaLabel)
  ) {
    return requestedAreaLabel;
  }

  if (
    normalizedDestination &&
    !isRecommendationTypeKeyword(normalizedDestination)
  ) {
    return normalizedDestination;
  }

  return "이 지역";
};

const STAY_COMMENT_TEMPLATES = [
  "{region} {typeObj} 골라봤어요",
  "{region} {typeObj} 추천해드려요",
  "{region}에 어울리는 {type}들 찾아봤어요",
  "{region}에서 괜찮은 {type}들 골라봤어요",
];

const attachObjectParticle = (word: string): string => {
  if (!word) return "";

  const lastChar = word[word.length - 1];
  const code = lastChar.charCodeAt(0);

  if (code < 0xac00 || code > 0xd7a3) {
    return word + "를";
  }

  const hasBatchim = (code - 0xac00) % 28 !== 0;
  return word + (hasBatchim ? "을" : "를");
};

const buildRecommendationComment = (
  originalMessage: string,
  destination: string,
  kind: RecommendationKind,
  items: RecommendationCardItem[]
): string => {
  const region = resolveRecommendationRegion(originalMessage, destination);

  if (kind === "restaurant") {
    return pickRandomTemplate(RESTAURANT_COMMENT_TEMPLATES).replace(
      "{region}",
      region
    );
  }

  const stayType = resolveStayDisplayType(originalMessage, items);
  const typeObj = attachObjectParticle(stayType);

  return pickRandomTemplate(STAY_COMMENT_TEMPLATES)
    .replace("{region}", region)
    .replace("{typeObj}", typeObj)
    .replace(/\{type\}/g, stayType);
};

const createRecommendationPayload = (
  originalMessage: string,
  destination: string,
  kind: RecommendationKind,
  rawItems: RecommendationItem[],
  summary = ""
): RecommendationPayload => {
  const normalizedItems = normalizeRecommendationCardItems(rawItems, kind);

  return {
    kind,
    title: buildRecommendationComment(
      safeText(originalMessage),
      safeText(destination),
      kind,
      normalizedItems
    ),
    summary,
    items: normalizedItems,
  };
};

const createRecommendationFormattedResponse = (
  originalMessage: string,
  destination: string,
  kind: RecommendationKind,
  rawItems: RecommendationItem[],
  summary = ""
): FormattedChatResponse => {
  const payload = createRecommendationPayload(
    originalMessage,
    destination,
    kind,
    rawItems,
    summary
  );

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

  return createRecommendationFormattedResponse(
    safeText(data.originalMessage),
    safeText(data.destination),
    kind,
    pickRecommendationItems(data),
    summary
  );
};

const buildCombinedSyntheticMessage = (
  destination: string,
  kind: RecommendationKind
): string => {
  const region = safeText(destination) || "이 지역";
  return kind === "restaurant"
    ? `${region} 맛집 추천`
    : `${region} 숙소 추천`;
};

const formatCombinedResponses = (data: ChatResponse): FormattedChatResponse[] => {
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
        buildCombinedSyntheticMessage(safeText(data.destination), "restaurant"),
        safeText(data.destination),
        "restaurant",
        combined?.restaurants ?? []
      )
    );
  }

  if (isNonEmptyArray(combined?.stays)) {
    responses.push(
      createRecommendationFormattedResponse(
        buildCombinedSyntheticMessage(safeText(data.destination), "stay"),
        safeText(data.destination),
        "stay",
        combined?.stays ?? []
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