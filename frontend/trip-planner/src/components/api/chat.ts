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
  displayType?: string;
  displayTitle?: string;
}

export interface CombinedItineraryResponse {
  dayPlans?: DayPlanResponse[];
  items?: RecommendationItem[];
}

export interface CombinedRecommendationResponse {
  summary?: string;
  itinerary?: CombinedItineraryResponse | null;
  restaurants?: RecommendationItem[];
  stays?: RecommendationItem[];
  attractions?: RecommendationItem[];
  restaurantDisplayTitle?: string;
  stayDisplayTitle?: string;
  attractionDisplayTitle?: string;
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