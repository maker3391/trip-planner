import type {
  ChatResponse,
  RecommendationItem,
  DayPlanResponse,
} from "../../api/chat";

const formatRecommendationItems = (items?: RecommendationItem[]): string => {
  if (!items || items.length === 0) return "";

  return items
    .map((item, index) => {
      const title = item.title || item.name || `추천 ${index + 1}`;
      const description =
        item.description || item.content || item.address || "";
      const category = item.category ? ` (${item.category})` : "";

      return `${index + 1}. ${title}${category}${
        description ? `\n- ${description}` : ""
      }`;
    })
    .join("\n\n");
};

const formatDayPlans = (dayPlans?: DayPlanResponse[]): string[] => {
  if (!dayPlans?.length) return [];

  const lines: string[] = [];

  dayPlans.forEach((dayPlan) => {
    lines.push(`📅 Day ${dayPlan.day}`);

    if (dayPlan.places?.length) {
      dayPlan.places.forEach((place, index) => {
        lines.push(`${index + 1}. ${place}`);
      });
    }

    lines.push("");
  });

  return lines;
};

export const formatChatResponse = (data: ChatResponse): string => {
  const lines: string[] = [];

  if (data.destination) {
    lines.push(`여행지: ${data.destination}`);
  }

  if (data.days) {
    lines.push(`기간: ${data.days}일`);
  }

  if (lines.length > 0) {
    lines.push("");
  }

  if (data.recommendation?.summary) {
    lines.push(data.recommendation.summary);
    lines.push("");
  }

  lines.push(...formatDayPlans(data.recommendation?.dayPlans));

  const recommendationItems =
    data.recommendation?.items ||
    data.recommendation?.recommendations ||
    data.recommendation?.places ||
    data.recommendation?.restaurants ||
    data.recommendation?.hotels;

  const formattedRecommendationItems =
    formatRecommendationItems(recommendationItems);

  if (formattedRecommendationItems) {
    lines.push(formattedRecommendationItems);
    lines.push("");
  }

  if (data.combinedRecommendation?.summary) {
    lines.push(data.combinedRecommendation.summary);
    lines.push("");
  }

  if (data.combinedRecommendation?.itinerary) {
    lines.push(data.combinedRecommendation.itinerary);
    lines.push("");
  }

  const combinedItems =
    data.combinedRecommendation?.items ||
    data.combinedRecommendation?.recommendations;

  const formattedCombinedItems = formatRecommendationItems(combinedItems);

  if (formattedCombinedItems) {
    lines.push(formattedCombinedItems);
    lines.push("");
  }

  const result = lines.join("\n").trim();
  return result || "응답은 받았지만 표시할 내용이 없습니다.";
};