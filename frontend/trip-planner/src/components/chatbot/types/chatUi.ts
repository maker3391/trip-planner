export type RecommendationKind = "restaurant" | "stay" | "attraction";

export interface RecommendationCardItem {
  id: string;
  title: string;
  category: string;
  description: string;
  address: string;
  link: string;
}

export interface RecommendationPayload {
  kind: RecommendationKind;
  title: string;
  summary: string;
  items: RecommendationCardItem[];
}

export interface CombinedRecommendationPayload {
  title: string;
  itineraryContent: string;
  restaurants?: RecommendationPayload;
  stays?: RecommendationPayload;
  attractions?: RecommendationPayload;
}

export interface ChatMessage {
  id: number;
  role: "user" | "assistant";
  content: string;
  variant?: "default" | "welcome" | "itinerary" | "recommendation" | "error";
  payload?: RecommendationPayload;
}