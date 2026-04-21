import { useState } from "react";
import { sendChatMessage } from "../../api/chat";
import type { ChatResponse } from "../../api/chat";
import type { ChatMessage } from "../types/chatUi";
import { formatChatResponse } from "../utils/chatFormatter";
import { extractChatErrorMessage } from "../utils/chatError";
import {
  createAssistantMessage,
  createUserMessage,
} from "../utils/chatMessage";
import { WELCOME_MESSAGE } from "../constants/chatConstants";

export default function useChatMessages() {
  const [messages, setMessages] = useState<ChatMessage[]>([WELCOME_MESSAGE]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [typingMessageId, setTypingMessageId] = useState<number | null>(null);
  const [animatedMessageIds, setAnimatedMessageIds] = useState<number[]>([]);

  const sendMessage = async (rawInput: string): Promise<void> => {
    const trimmed = rawInput.trim();

    if (!trimmed || isLoading) return;

    const userMessage = createUserMessage(trimmed);

    setMessages((prev) => [...prev, userMessage]);
    setInput("");
    setIsLoading(true);

    try {
      const response: ChatResponse = await sendChatMessage({
        message: trimmed,
      });

      const formatted = formatChatResponse(response);

      const assistantMessage = createAssistantMessage(
        formatted.content,
        formatted.variant,
        formatted.payload
      );

      setTypingMessageId(assistantMessage.id);
      setMessages((prev) => [...prev, assistantMessage]);
    } catch (error: unknown) {
      console.error("챗봇 API 호출 실패:", error);

      const errorMessage = createAssistantMessage(
        extractChatErrorMessage(error),
        "error"
      );

      setTypingMessageId(errorMessage.id);
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleTypingEnd = (messageId: number): void => {
    setAnimatedMessageIds((prev) =>
      prev.includes(messageId) ? prev : [...prev, messageId]
    );

    setTypingMessageId((prev) => (prev === messageId ? null : prev));
  };

  return {
    messages,
    input,
    isLoading,
    typingMessageId,
    animatedMessageIds,
    setInput,
    sendMessage,
    handleTypingEnd,
  };
}