import type { ChatMessage } from "../types/chatUi";

export const WELCOME_MESSAGE: ChatMessage = {
  id: 1,
  role: "assistant",
  variant: "welcome",
  content:
    "여행 계획을 도와드릴게요 ✈️\n\n원하는 지역과 내용을 입력하면 추천해드려요\n\n아래처럼 입력해보세요\n\n• 제주도 2박 3일 여행 추천\n• 부산 해운대 맛집 추천\n• 서울 강남 숙소 추천",
};