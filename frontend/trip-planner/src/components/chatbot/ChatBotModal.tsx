import { useEffect, useRef, useState } from "react";
import "./ChatBot.css";
import { sendChatMessage, ChatResponse } from "../api/chat.ts";

interface ChatBotModalProps {
  open: boolean;
  onClose: () => void;
}

interface ChatMessage {
  id: number;
  role: "user" | "assistant";
  content: string;
}

export default function ChatBotModal({
  open,
  onClose,
}: ChatBotModalProps) {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 1,
      role: "assistant",
      content: "안녕하세요! 여행 계획에 대해 무엇이든 물어보세요.",
    },
  ]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const messageEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!open) return;
    messageEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, open]);

  const formatRecommendationItems = (items: any[] | undefined) => {
    if (!items || items.length === 0) return "";

    return items
      .map((item, index) => {
        const title = item.title || item.name || `추천 ${index + 1}`;
        const description =
          item.description || item.content || item.address || "";
        const category = item.category ? ` (${item.category})` : "";

        return `${index + 1}. ${title}${category}${description ? `\n- ${description}` : ""}`;
      })
      .join("\n\n");
  };

  const formatChatResponse = (data: ChatResponse) => {
    const lines: string[] = [];

    if (data.destination) {
      lines.push(`여행지: ${data.destination}`);
    }

    if (data.days) {
      lines.push(`기간: ${data.days}일`);
    }

    if (data.intent) {
      lines.push(`의도: ${data.intent}`);
    }

    if (lines.length > 0) {
      lines.push("");
    }

    if (data.recommendation?.summary) {
      lines.push(data.recommendation.summary);
      lines.push("");
    }

    const recommendationItems =
      data.recommendation?.items ||
      data.recommendation?.recommendations ||
      data.recommendation?.places ||
      data.recommendation?.restaurants ||
      data.recommendation?.hotels;
    
    if (data.recommendation?.dayPlans?.length) {
      data.recommendation.dayPlans.forEach((dayPlan) => {
        lines.push(`📅 Day ${dayPlan.day}`);

        if (dayPlan.places?.length) {
          dayPlan.places.forEach((place, index) => {
            lines.push(`${index + 1}. ${place}`);
          });
        }

        lines.push("");
      });
    }

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

  const handleSend = async () => {
    const trimmed = input.trim();

    if (!trimmed || isLoading) return;

    const userMessage: ChatMessage = {
      id: Date.now(),
      role: "user",
      content: trimmed,
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput("");
    setIsLoading(true);

    try {
      const response = await sendChatMessage({ message: trimmed });

      const botMessage: ChatMessage = {
        id: Date.now() + 1,
        role: "assistant",
        content: formatChatResponse(response),
      };

      setMessages((prev) => [...prev, botMessage]);
    } catch (error: any) {
      console.error("챗봇 API 호출 실패:", error);

      let errorMessage = "죄송합니다. 답변을 불러오는 중 오류가 발생했습니다.";

      if (error?.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error?.message) {
        errorMessage = error.message;
      }

      const botMessage: ChatMessage = {
        id: Date.now() + 1,
        role: "assistant",
        content: errorMessage,
      };

      setMessages((prev) => [...prev, botMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && !e.nativeEvent.isComposing) {
      handleSend();
    }
  };

  if (!open) return null;

  return (
    <div className="chatbot-overlay">
      <div className="chatbot-modal">
        <div className="chatbot-header">
          <div className="chatbot-header-text">
            <h3>AI 여행 도우미</h3>
            <span>여행 일정과 장소를 편하게 물어보세요</span>
          </div>

          <button
            className="chatbot-close"
            onClick={onClose}
            type="button"
            aria-label="챗봇 닫기"
          >
            ×
          </button>
        </div>

        <div className="chatbot-body">
          {messages.map((message) => (
            <div
              key={message.id}
              className={`chat-message ${message.role === "user" ? "user" : "assistant"}`}
            >
              <div className="chat-bubble">
                {message.content.split("\n").map((line, index) => (
                  <p key={index}>{line}</p>
                ))}
              </div>
            </div>
          ))}

          {isLoading && (
            <div className="chat-message assistant">
              <div className="chat-bubble loading">
                <p>답변 작성 중...</p>
              </div>
            </div>
          )}

          <div ref={messageEndRef} />
        </div>

        <div className="chatbot-input-area">
          <input
            type="text"
            placeholder="메시지를 입력하세요"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <button onClick={handleSend} type="button" disabled={isLoading}>
            전송
          </button>
        </div>
      </div>
    </div>
  );
}