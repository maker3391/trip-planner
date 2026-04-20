import type { KeyboardEvent } from "react";

interface ChatInputProps {
  value: string;
  isLoading: boolean;
  onChange: (value: string) => void;
  onSend: () => void;
}

export default function ChatInput({
  value,
  isLoading,
  onChange,
  onSend,
}: ChatInputProps) {
  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>): void => {
    if (e.key === "Enter" && !e.nativeEvent.isComposing) {
      onSend();
    }
  };

  return (
    <div className="chatbot-input-area">
      <input
        type="text"
        placeholder="여행에 대해 물어보세요"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onKeyDown={handleKeyDown}
        disabled={isLoading}
      />

      <button
        onClick={onSend}
        type="button"
        disabled={isLoading}
        aria-label={isLoading ? "답변 생성 중" : "메시지 전송"}
      >
        {isLoading ? (
          <span className="btn-spinner" aria-hidden="true" />
        ) : (
          <svg
            className="send-icon"
            viewBox="0 0 24 24"
            fill="none"
            aria-hidden="true"
          >
            <path
              d="M3 20L21 12L3 4V10L15 12L3 14V20Z"
              fill="currentColor"
            />
          </svg>
        )}
      </button>
    </div>
  );
}