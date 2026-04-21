import type { ChatMessage } from "../types/chatUi";

export const WELCOME_MESSAGE: ChatMessage = {
  id: 1,
  role: "assistant",
  variant: "welcome",
  content: "안녕하세요!\n여행 일정, 맛집, 숙소를 추천해드릴게요\n\n아래처럼 입력해보세요\n\n• 제주도 2박 3일 여행 추천\n• 부산 해운대 맛집 추천\n• 서울 강남 숙소 추천",
};