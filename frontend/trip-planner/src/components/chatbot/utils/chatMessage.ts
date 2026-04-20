import type { ChatMessage } from "../types/chatUi";

export const createUserMessage = (content: string): ChatMessage => ({
  id: Date.now(),
  role: "user",
  content,
});

export const createAssistantMessage = (content: string): ChatMessage => ({
  id: Date.now() + 1,
  role: "assistant",
  content,
});