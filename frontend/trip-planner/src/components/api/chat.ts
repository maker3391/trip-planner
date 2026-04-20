import client from "./client";

export interface RecommendationItem {
  title?: string;
  name?: string;
  description?: string;
  content?: string;
  address?: string;
  category?: string;
  placeUrl?: string;
  link?: string;
}

export interface DayPlanResponse {
  day: number;
  summary?: string | null;
  places?: string[];
}

export interface RecommendationContentResponse {
  summary?: string;
  items?: RecommendationItem[];
  recommendations?: RecommendationItem[];
  places?: RecommendationItem[];
  restaurants?: RecommendationItem[];
  hotels?: RecommendationItem[];
  dayPlans?: DayPlanResponse[];
}

export interface CombinedRecommendationResponse {
  summary?: string;
  itinerary?: string;
  items?: RecommendationItem[];
  recommendations?: RecommendationItem[];
}

export interface ChatRequest {
  message: string;
}

export interface ChatResponse {
  originalMessage: string;
  intent?: string;
  destination?: string;
  days?: number;
  recommendation?: RecommendationContentResponse | null;
  combinedRecommendation?: CombinedRecommendationResponse | null;
}

export const sendChatMessage = async (
  request: ChatRequest
): Promise<ChatResponse> => {
  const response = await client.post<ChatResponse>("/chat", request);
  return response.data;
};