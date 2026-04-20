import { useState } from "react";
import type { ChatResponse } from "../../api/chat";
import { sendChatMessage } from "../../api/chat";
import type { ChatMessage } from "../types/chatUi";
import { formatChatResponse } from "../utils/chatFormatter";
import { extractChatErrorMessage } from "../utils/chatError";
import {
  createUserMessage,
  createAssistantMessage,
} from "../utils/chatMessage";
import { WELCOME_MESSAGE } from "../constants/chatConstants";

export default function useChatMessages() {
  const [messages, setMessages] = useState<ChatMessage[]>([
    WELCOME_MESSAGE,
  ]);

  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [typingMessageId, setTypingMessageId] = useState<number | null>(null);
  const [animatedMessageIds, setAnimatedMessageIds] = useState<number[]>([]);

  const handleTypingEnd = (messageId: number) => {
    setAnimatedMessageIds((prev) =>
      prev.includes(messageId) ? prev : [...prev, messageId]
    );

    setTypingMessageId((prev) =>
      prev === messageId ? null : prev
    );
  };

  const sendMessage = async (text: string) => {
    const trimmed = text.trim();
    if (!trimmed || isLoading) return;

    const userMessage = createUserMessage(trimmed);

    setMessages((prev) => [...prev, userMessage]);
    setInput("");
    setIsLoading(true);

    try {
      const response: ChatResponse = await sendChatMessage({
        message: trimmed,
      });

      const botMessage = createAssistantMessage(
        formatChatResponse(response)
      );

      setTypingMessageId(botMessage.id);
      setMessages((prev) => [...prev, botMessage]);
    } catch (error: unknown) {
      const botMessage = createAssistantMessage(
        extractChatErrorMessage(error)
      );

      setTypingMessageId(botMessage.id);
      setMessages((prev) => [...prev, botMessage]);
    } finally {
      setIsLoading(false);
    }
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