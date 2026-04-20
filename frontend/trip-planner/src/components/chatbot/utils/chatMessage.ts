import type { ChatMessage, RecommendationPayload } from "../types/chatUi";

const createMessageId = (): number =>
  Date.now() + Math.floor(Math.random() * 1000);

export const createUserMessage = (content: string): ChatMessage => ({
  id: createMessageId(),
  role: "user",
  content,
  variant: "default",
});

export const createAssistantMessage = (
  content: string,
  variant: ChatMessage["variant"] = "default",
  payload?: RecommendationPayload
): ChatMessage => ({
  id: createMessageId(),
  role: "assistant",
  content,
  variant,
  payload,
});