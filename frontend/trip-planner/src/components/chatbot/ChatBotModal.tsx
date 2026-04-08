import { useEffect, useRef, useState } from "react";
import "./ChatBot.css";

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

  const handleSend = () => {
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

    setTimeout(() => {
      const botMessage: ChatMessage = {
        id: Date.now() + 1,
        role: "assistant",
        content:
          `지금은 챗봇 UI 테스트 단계예요.\n` +
          `백엔드 API 연결 전이라 더미 응답을 보여주고 있어요.\n` +
          `입력한 질문: "${trimmed}"`,
      };

      setMessages((prev) => [...prev, botMessage]);
      setIsLoading(false);
    }, 700);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      handleSend();
    }
  };

  const handleOverlayClick = () => {
    onClose();
  };

  const handleModalClick = (e: React.MouseEvent<HTMLDivElement>) => {
    e.stopPropagation();
  };

  if (!open) return null;

  return (
    <div className="chatbot-overlay" onClick={handleOverlayClick}>
      <div className="chatbot-modal" onClick={handleModalClick}>
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